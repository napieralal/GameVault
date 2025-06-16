package com.example.gamevault.ui.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.model.Game
import com.example.gamevault.data.remote.ApiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody

enum class SortField {
    RELEVANCE, RATING, NAME, RELEASE_DATE, POPULARITY
}

enum class SortDirection {
    ASC, DESC
}

data class SortType(
    val field: SortField = SortField.RELEVANCE,
    val direction: SortDirection = SortDirection.DESC
)

data class SearchFilters(
    val query: String = "",
    val selectedGenreIds: List<Int> = emptyList(),
    val selectedPlatformIds: List<Int> = emptyList(),
    val sortType: SortType = SortType(),
    val selectedModeIds: List<Int> = emptyList(),
    val selectedPerspectiveIds: List<Int> = emptyList(),
    val ratingRange: IntRange = 0..100,
    val yearRange: IntRange = 1947..2030,
)

enum class FilterType {
    CHECKBOX, RANGE, DROPDOWN
}

data class FilterItem(val id: Int, val label: String)

data class FilterSection(
    val title: String,
    val type: FilterType,
    val items: List<FilterItem> = emptyList(),
    val selectedIds: List<Int> = emptyList(),
    val onToggle: ((Int) -> Unit)? = null,
    val range: IntRange? = null,
    val selectedRange: IntRange? = null,
    val onRangeChange: ((IntRange) -> Unit)? = null
)

sealed interface SearchUiState {
    object Initial : SearchUiState
    object Loading : SearchUiState
    data class Success(val games: List<Game>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    savedStateHandle: SavedStateHandle,
    private val searchApiService: ApiService
) : ViewModel() {

    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private val pageSize = 15

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _endReached = MutableStateFlow(false)
    val endReached: StateFlow<Boolean> = _endReached

    private val _pendingFilters = MutableStateFlow(SearchFilters())

    init {
        observeFiltersAndFetchGames()
    }

    private fun observeFiltersAndFetchGames() {
        viewModelScope.launch {
            filters
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { filters ->
                    currentPage = 0
                    _endReached.value = false
                    _uiState.value = SearchUiState.Loading
                    try {
                        val games = fetchGames(filters, page = 0)
                        _uiState.value = SearchUiState.Success(games)
                    } catch (e: Exception) {
                        _uiState.value = SearchUiState.Error("Error: ${e.message}")
                    }
                }
        }
    }

    private suspend fun fetchGames(filters: SearchFilters, page: Int): List<Game> {
        val offset = page * pageSize
        val sortField = if (filters.query.isNotBlank() || filters.sortType.field == SortField.RELEVANCE) {
            null
        } else {
            val fieldName = when (filters.sortType.field) {
                SortField.RATING -> "total_rating"
                SortField.NAME -> "name"
                SortField.RELEASE_DATE -> "first_release_date"
                SortField.POPULARITY -> "rating_count"
                SortField.RELEVANCE -> ""
            }
            "$fieldName ${filters.sortType.direction.name.lowercase()}"
        }

        val startYearTimestamp = yearToUnixTimestamp(filters.yearRange.first)
        val endYearTimestamp = yearToUnixTimestamp(filters.yearRange.last, endOfYear = true)

        val queryBuilder = StringBuilder().apply {
            append("fields name, genres.name, platforms.name, cover.image_id, total_rating, rating_count, first_release_date, game_type; ")
            append("limit $pageSize; offset $offset; ")

            // SEARCH
            if (filters.query.isNotBlank()) {
                append("search \"${filters.query}\"; ")
            }

            // WHERE CONDITIONS
            val whereConditions = mutableListOf<String>()

            if (filters.selectedGenreIds.isNotEmpty()) {
                whereConditions.add("(genres = (${filters.selectedGenreIds.joinToString()}) | genres = null)")
            }

            if (filters.selectedPlatformIds.isNotEmpty()) {
                whereConditions.add("(platforms = (${filters.selectedPlatformIds.joinToString()}) | platforms = null)")
            }

            if (filters.selectedModeIds.isNotEmpty()) {
                whereConditions.add("(game_modes = (${filters.selectedModeIds.joinToString()}) | game_modes = null)")
            }

            if (filters.selectedPerspectiveIds.isNotEmpty()) {
                whereConditions.add("(player_perspectives = (${filters.selectedPerspectiveIds.joinToString()}) | player_perspectives = null)")
            }

            // Rating Range
            whereConditions.add("(total_rating >= ${filters.ratingRange.first} & total_rating <= ${filters.ratingRange.last} | total_rating = null)")

            // Year Range (converted to timestamps)
            whereConditions.add("(first_release_date = null | (first_release_date >= $startYearTimestamp & first_release_date <= $endYearTimestamp))")

            if (whereConditions.isNotEmpty()) {
                append("where ${whereConditions.joinToString(" & ")}; ")
            }

            if (sortField != null) {
                append("sort $sortField; ")
            }
        }

        return searchApiService.getGames(queryBuilder.toString().toRequestBody())
    }

    private fun yearToUnixTimestamp(year: Int, endOfYear: Boolean = false): Long {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, if (endOfYear) java.util.Calendar.DECEMBER else java.util.Calendar.JANUARY)
            set(java.util.Calendar.DAY_OF_MONTH, if (endOfYear) 31 else 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis / 1000L
    }

    fun updateQuery(query: String) {
        _filters.update { it.copy(query = query) }
    }

    fun updateSort(sort: SortType) {
        _filters.update { it.copy(sortType = sort) }
    }

    fun loadMore() {
        if (_isLoadingMore.value || uiState.value !is SearchUiState.Success) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val moreGames = fetchGames(_filters.value, page = currentPage + 1)
                val currentGames = (uiState.value as SearchUiState.Success).games

                val uniqueNewGames = moreGames.filterNot { new ->
                    currentGames.any { existing -> existing.id == new.id }
                }

                if (uniqueNewGames.isNotEmpty()) {
                    currentPage++
                    _uiState.value = SearchUiState.Success(currentGames + uniqueNewGames)
                    if (uniqueNewGames.size < pageSize) {
                        _endReached.value = true
                    }
                } else {
                    _endReached.value = true
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error("Wystąpił błąd podczas pobierania danych")
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun getFilterSections(
        filters: SearchFilters,
        onChange: (SearchFilters) -> Unit
    ): List<FilterSection> {
        fun toggle(list: List<Int>, id: Int): List<Int> {
            return if (id in list) list - id else list + id
        }

        return listOf(
            FilterSection(
                title = "Genres",
                type = FilterType.CHECKBOX,
                items = listOf(
                    FilterItem(30, "Pinball"),
                    FilterItem(31, "Adventure"),
                    FilterItem(32, "Indie"),
                    FilterItem(33, "Arcade"),
                    FilterItem(34, "Visual Novel"),
                    FilterItem(35, "Card & Board Game"),
                    FilterItem(36, "MOBA"),
                    FilterItem(2, "Point-and-click"),
                    FilterItem(4, "Fighting"),
                    FilterItem(5, "Shooter"),
                    FilterItem(7, "Music"),
                    FilterItem(8, "Platform"),
                    FilterItem(9, "Puzzle"),
                    FilterItem(10, "Racing"),
                    FilterItem(11, "Real Time Strategy (RTS)"),
                    FilterItem(12, "Role-playing (RPG)"),
                    FilterItem(13, "Simulator"),
                    FilterItem(14, "Sport"),
                    FilterItem(15, "Strategy"),
                    FilterItem(16, "Turn-based strategy (TBS)"),
                    FilterItem(17, "Tactical"),
                    FilterItem(18, "Hack and slash/Beat 'em up"),
                    FilterItem(26, "Quiz/Trivia")
                ),
                selectedIds = filters.selectedGenreIds,
                onToggle = { id ->
                    onChange(filters.copy(selectedGenreIds = toggle(filters.selectedGenreIds, id)))
                }
            ),
            FilterSection(
                title = "Platforms",
                type = FilterType.CHECKBOX,
                items = listOf(
                    FilterItem(14, "Mac"),
                    FilterItem(6, "PC (Microsoft Windows)"),
                    FilterItem(3, "Linux"),
                    FilterItem(167, "PlayStation 5"),
                    FilterItem(169, "Xbox Series X|S"),
                    FilterItem(49, "Xbox One"),
                    FilterItem(48, "PlayStation 4"),
                    FilterItem(130, "Nintendo Switch"),
                    FilterItem(46, "PlayStation 3"),
                    FilterItem(45, "Xbox 360")
                ),
                selectedIds = filters.selectedPlatformIds,
                onToggle = { id ->
                    onChange(filters.copy(selectedPlatformIds = toggle(filters.selectedPlatformIds, id)))
                }
            ),
            FilterSection(
                title = "Release Year",
                type = FilterType.RANGE,
                range = 1980..2025,
                selectedRange = filters.yearRange,
                onRangeChange = { newRange ->
                    onChange(filters.copy(yearRange = newRange))
                }
            ),
            FilterSection(
                title = "Rating",
                type = FilterType.RANGE,
                range = 0..100,
                selectedRange = filters.ratingRange,
                onRangeChange = { newRange ->
                    onChange(filters.copy(ratingRange = newRange))
                }
            ),
            FilterSection(
                title = "Modes",
                type = FilterType.CHECKBOX,
                items = listOf(
                    FilterItem(1, "Singleplayer"),
                    FilterItem(2, "Multiplayer"),
                    FilterItem(3, "Co-operative"),
                    FilterItem(4, "Split screen")
                ),
                selectedIds = filters.selectedModeIds,
                onToggle = { id ->
                    onChange(filters.copy(selectedModeIds = toggle(filters.selectedModeIds, id)))
                }
            ),
            FilterSection(
                title = "Perspective",
                type = FilterType.CHECKBOX,
                items = listOf(
                    FilterItem(1, "First-person"),
                    FilterItem(2, "Third-person"),
                    FilterItem(3, "Text"),
                    FilterItem(4, "Side view"),
                    FilterItem(5, "Virtual Reality"),
                    FilterItem(6, "Bird view / Isometric"),
                    FilterItem(7, "Auditory")
                ),
                selectedIds = filters.selectedPerspectiveIds,
                onToggle = { id ->
                    onChange(filters.copy(selectedPerspectiveIds = toggle(filters.selectedPerspectiveIds, id)))
                }
            )
        )
    }

    fun updateFilters(newFilters: SearchFilters) {
        _filters.value = newFilters
    }

    fun updateSort(field: SortField) {
        val current = _filters.value.sortType
        val newDirection = if (current.field == field) {
            if (current.direction == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC
        } else {
            SortDirection.DESC
        }
        _filters.update {
            it.copy(sortType = SortType(field, newDirection))
        }
    }

    fun toggleSortDirection() {
        val current = filters.value.sortType
        val newDirection = if (current.direction == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC
        updateSort(current.copy(direction = newDirection))
    }
}


