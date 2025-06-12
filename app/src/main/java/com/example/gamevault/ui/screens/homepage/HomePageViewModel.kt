package com.example.gamevault.ui.screens.homepage

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.model.Game
import com.example.gamevault.network.ApiService
import com.example.gamevault.ui.screens.search.SearchUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.collections.plus

data class GenreWithGameCover(
    val genreName: String,
    val genreId: Int,
    val coverImageId: String?,
    val gameName: String
)

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val popular: List<Game>,
        val newReleases: List<Game>,
        val topRated: List<Game>,
        val upcoming: List<Game>,
        //val genreChips: List<GenreWithGameCover>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

enum class GameSection {
    POPULAR, NEW_RELEASES, TOP_RATED, UPCOMING
}

class HomePageViewModel(
    savedStateHandle: SavedStateHandle,
    private val homeApiService: ApiService
) : ViewModel() {

    private val savedState = savedStateHandle

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _popularGames = mutableStateListOf<Game>().apply {
        addAll(savedState.get<List<Game>>("popularGames") ?: emptyList())
    }
    private val _newReleases = mutableStateListOf<Game>().apply {
        addAll(savedState.get<List<Game>>("newReleases") ?: emptyList())
    }
    private val _topRated = mutableStateListOf<Game>().apply {
        addAll(savedState.get<List<Game>>("topRated") ?: emptyList())
    }
    private val _upcoming = mutableStateListOf<Game>().apply {
        addAll(savedState.get<List<Game>>("upcoming") ?: emptyList())
    }

    private var currentPopularPage = savedState.get<Int>("currentPopularPage") ?: 0
    private var currentNewReleasesPage = savedState.get<Int>("currentNewReleasesPage") ?: 0
    private var currentTopRatedPage = savedState.get<Int>("currentTopRatedPage") ?: 0
    private var currentUpcomingPage = savedState.get<Int>("currentUpcomingPage") ?: 0

    private var initialLoadDone = savedState.get<Boolean>("initialLoadDone") ?: false


    init {
        if (!initialLoadDone) {
            fetchInitialData()
        } else if (_uiState.value is HomeUiState.Loading) {
            _uiState.value = HomeUiState.Success(
                popular = _popularGames,
                newReleases = _newReleases,
                topRated = _topRated,
                upcoming = _upcoming
            )
        }
    }

    private fun saveState() {
        savedState["uiState"] = _uiState.value
        savedState["popularGames"] = _popularGames.toList()
        savedState["newReleases"] = _newReleases.toList()
        savedState["topRated"] = _topRated.toList()
        savedState["upcoming"] = _upcoming.toList()
        savedState["currentPopularPage"] = currentPopularPage
        savedState["currentNewReleasesPage"] = currentNewReleasesPage
        savedState["currentTopRatedPage"] = currentTopRatedPage
        savedState["currentUpcomingPage"] = currentUpcomingPage
        savedState["initialLoadDone"] = initialLoadDone
    }

    private fun fetchInitialData() {
        if (initialLoadDone) return

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Ładujemy pierwsze strony równolegle
                val popularDeferred = async { fetchGames("rating_count desc", limit = 10) }
                val newReleasesDeferred = async { fetchGames("first_release_date desc", releasedOnly = true, limit = 10) }
                val topRatedDeferred = async { fetchGames("total_rating desc", limit = 10) }
                val upcomingDeferred = async { fetchGames("first_release_date asc", futureOnly = true, limit = 10) }

                _popularGames.addAll(popularDeferred.await())
                _newReleases.addAll(newReleasesDeferred.await())
                _topRated.addAll(topRatedDeferred.await())
                _upcoming.addAll(upcomingDeferred.await())

                _uiState.value = HomeUiState.Success(
                    popular = _popularGames,
                    newReleases = _newReleases,
                    topRated = _topRated,
                    upcoming = _upcoming
                )
                initialLoadDone = true
                saveState()
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to load games: ${e.message}")
                saveState()
            }
        }
    }

    fun loadMore(section: GameSection) {
        if (_isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val newGames = when (section) {
                    GameSection.POPULAR -> {
                        currentPopularPage++
                        fetchGames(
                            sort = "rating_count desc",
                            offset = currentPopularPage * 10,
                            limit = 10
                        )
                    }
                    GameSection.NEW_RELEASES -> {
                        currentNewReleasesPage++
                        fetchGames(
                            sort = "first_release_date desc",
                            offset = currentNewReleasesPage * 10,
                            limit = 10,
                            releasedOnly = true
                        )
                    }
                    GameSection.TOP_RATED -> {
                        currentTopRatedPage++
                        fetchGames(
                            sort = "total_rating desc",
                            offset = currentTopRatedPage * 10,
                            limit = 10
                        )
                    }
                    GameSection.UPCOMING -> {
                        currentUpcomingPage++
                        fetchGames(
                            sort = "first_release_date asc",
                            offset = currentUpcomingPage * 10,
                            limit = 10,
                            futureOnly = true
                        )
                    }
                }

                when (section) {
                    GameSection.POPULAR -> _popularGames.addAll(newGames)
                    GameSection.NEW_RELEASES -> _newReleases.addAll(newGames)
                    GameSection.TOP_RATED -> _topRated.addAll(newGames)
                    GameSection.UPCOMING -> _upcoming.addAll(newGames)
                }

                _uiState.value = HomeUiState.Success(
                    popular = _popularGames,
                    newReleases = _newReleases,
                    topRated = _topRated,
                    upcoming = _upcoming
                )
                saveState()
            } catch (e: Exception) {
                // Możesz dodać obsługę błędów
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private suspend fun fetchGames(
        sort: String,
        offset: Int = 0,
        limit: Int = 10,
        releasedOnly: Boolean = false,
        futureOnly: Boolean = false
    ): List<Game> {
        val nowUnix = System.currentTimeMillis() / 1000
        val whereConditions = mutableListOf(
            "cover != null",
            "first_release_date != null"
        )

        if (releasedOnly) {
            whereConditions.add("first_release_date <= $nowUnix")
        } else if (futureOnly) {
            whereConditions.add("first_release_date > $nowUnix")
        }

        val query = buildString {
            append("fields name, cover.image_id, total_rating, rating_count, first_release_date, platforms.name, genres.name; ")
            append("sort $sort; ")
            append("limit $limit; ")
            append("offset $offset; ")
            append("where ${whereConditions.joinToString(" & ")};")
        }

        return homeApiService.getGames(query.toRequestBody())
    }


    /*private suspend fun fetchGenreWithTopGame(): List<GenreWithGameCover> {
        val result = mutableListOf<GenreWithGameCover>()

        for ((name, id) in genreMap) {
            val query = """
            fields name, cover.image_id;
            where genres = $id & cover != null;
            sort popularity desc;
            limit 1;
        """.trimIndent()

            try {
                val games = homeApiService.getGames(query.toRequestBody())
                games.firstOrNull()?.let { game ->
                    result.add(
                        GenreWithGameCover(
                            genreName = name,
                            genreId = id,
                            coverImageId = game.cover?.imageId,
                            gameName = game.name ?: "Unknown"
                        )
                    )
                }
            } catch (e: Exception) {
                println("Error fetching top game for genre $name: ${e.message}")
            }
        }

        return result
    }*/

    fun refresh() {
        _popularGames.clear()
        _newReleases.clear()
        _topRated.clear()
        _upcoming.clear()
        currentPopularPage = 0
        currentNewReleasesPage = 0
        currentTopRatedPage = 0
        currentUpcomingPage = 0
        initialLoadDone = false
        fetchInitialData()
    }
}
