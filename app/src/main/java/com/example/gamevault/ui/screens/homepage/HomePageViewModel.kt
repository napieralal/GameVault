package com.example.gamevault.ui.screens.homepage

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.model.Game
import com.example.gamevault.model.GameStatus
import com.example.gamevault.model.UserGameEntity
import com.example.gamevault.data.remote.ApiService
import com.example.gamevault.data.remote.FirebaseLibraryService
import com.example.gamevault.data.local.GameVaultDatabase
import com.example.gamevault.data.repository.LibraryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody

data class GenreWithGameCover(
    val genreName: String,
    val genreId: Int,
    val coverImageId: String?,
    val gameName: String,
)

data class UserStats(
    val total: Int,
    val wantToPlay: Int,
    val playing: Int,
    val completed: Int,
    val genreCounts: Map<String, Int>
)

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val popular: List<Game>,
        val newReleases: List<Game>,
        val topRated: List<Game>,
        val upcoming: List<Game>,
        val upcomingFromLibrary: List<Game> = emptyList(),
        val recommended: List<Game> = emptyList(),
        val userStats: UserStats? = null
        //val genreChips: List<GenreWithGameCover>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

enum class GameSection {
    POPULAR, NEW_RELEASES, TOP_RATED, UPCOMING
}

class HomePageViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val homeApiService: ApiService
) : ViewModel() {
    private val db = GameVaultDatabase.getDatabase(application)
    private val firebaseService = FirebaseLibraryService()
    private val libraryRepository = LibraryRepository(db.userGameDao(), firebaseService)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _popularGames = mutableStateListOf<Game>()
    private val _newReleases = mutableStateListOf<Game>()
    private val _topRated = mutableStateListOf<Game>()
    private val _upcoming = mutableStateListOf<Game>()
    private val _recommended = mutableStateListOf<Game>()

    private var currentPopularPage = 0
    private var currentNewReleasesPage = 0
    private var currentTopRatedPage = 0
    private var currentUpcomingPage = 0
    private var currentRecommendedPage = 0

    private var initialLoadDone = false

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        if (initialLoadDone) return

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val userLibraryGames = fetchUserLibraryGamesFromApi()
                val now = System.currentTimeMillis() / 1000
                val thirtyDaysFromNow = now + (30 * 24 * 60 * 60)

                val upcomingFromLibrary = userLibraryGames.filter {
                    it.first_release_date?.let { date ->
                        date in now..thirtyDaysFromNow
                    } ?: false
                }

                val userGameEntities = libraryRepository.getAllGames()

                val userStats = UserStats(
                    total = userLibraryGames.size,
                    genreCounts = userLibraryGames
                        .flatMap { it.genres.orEmpty() }
                        .groupingBy { it.name }
                        .eachCount(),
                    wantToPlay = countByStatus(userGameEntities, GameStatus.WANT_TO_PLAY),
                    playing = countByStatus(userGameEntities, GameStatus.PLAYING),
                    completed = countByStatus(userGameEntities, GameStatus.COMPLETED)
                )

                val popularDeferred = async { fetchGames("rating_count desc", limit = 10) }
                val newReleasesDeferred = async { fetchGames("first_release_date desc", releasedOnly = true, limit = 10) }
                val topRatedDeferred = async { fetchGames("total_rating desc", limit = 10) }
                val upcomingDeferred = async { fetchGames("first_release_date asc", futureOnly = true, limit = 10) }

                _popularGames.addAll(popularDeferred.await())
                _newReleases.addAll(newReleasesDeferred.await())
                _topRated.addAll(topRatedDeferred.await())
                _upcoming.addAll(upcomingDeferred.await())

                val recommended = generateRecommendations(userLibraryGames)

                _uiState.value = HomeUiState.Success(
                    popular = _popularGames,
                    newReleases = _newReleases,
                    topRated = _topRated,
                    upcoming = _upcoming,
                    upcomingFromLibrary = upcomingFromLibrary,
                    recommended = recommended,
                    userStats = userStats
                )

                initialLoadDone = true
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to load games: ${e.message}")
            }
        }
    }

    fun refreshAfterSync() {
        viewModelScope.launch {
            try {
                val userLibraryGames = fetchUserLibraryGamesFromApi()
                val now = System.currentTimeMillis() / 1000
                val thirtyDaysFromNow = now + (30 * 24 * 60 * 60)

                val upcomingFromLibrary = userLibraryGames.filter {
                    it.first_release_date?.let { date ->
                        date in now..thirtyDaysFromNow
                    } ?: false
                }

                val userGameEntities = libraryRepository.getAllGames()

                val userStats = UserStats(
                    total = userLibraryGames.size,
                    genreCounts = userLibraryGames
                        .flatMap { it.genres.orEmpty() }
                        .groupingBy { it.name }
                        .eachCount(),
                    wantToPlay = countByStatus(userGameEntities, GameStatus.WANT_TO_PLAY),
                    playing = countByStatus(userGameEntities, GameStatus.PLAYING),
                    completed = countByStatus(userGameEntities, GameStatus.COMPLETED)
                )

                val recommended = generateRecommendations(userLibraryGames)

                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Success(
                        popular = currentState.popular,
                        newReleases = currentState.newReleases,
                        topRated = currentState.topRated,
                        upcoming = currentState.upcoming,
                        upcomingFromLibrary = upcomingFromLibrary,
                        recommended = recommended,
                        userStats = userStats
                    )
                }
            } catch (e: Exception) {
                println("Error refreshing user data: ${e.message}")
            }
        }
    }

    private suspend fun fetchUserLibraryGamesFromApi(): List<Game> {
        return try {
            val ids = libraryRepository.getAllGameIds()
            println("IDs from user library: $ids")
            if (ids.isEmpty()) return emptyList()

            val query = buildString {
                append("fields name, cover.image_id, total_rating, first_release_date, platforms.name, genres.name; ")
                append("where id = (${ids.joinToString(",")}); ")
                append("limit ${ids.size};")
            }

            homeApiService.getGames(query.toRequestBody())
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun generateRecommendations(library: List<Game>): List<Game> {
        val favoriteGenre = library
            .flatMap { it.genres.orEmpty() }
            .groupingBy { it.name }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: return emptyList()

        val query = buildString {
            append("fields name, cover.image_id, total_rating, first_release_date, platforms.name, genres.name; ")
            append("where genres.name = \"$favoriteGenre\" & cover != null & first_release_date != null; ")
            append("sort total_rating desc; limit 10;")
        }

        return homeApiService.getGames(query.toRequestBody())
            .filterNot { game -> library.any { it.id == game.id } }
    }

    private fun countByStatus(userGames: List<UserGameEntity>, status: GameStatus): Int {
        return userGames.count { it.status.equals(status.name, ignoreCase = true) }
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

                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Success(
                        popular = _popularGames,
                        newReleases = _newReleases,
                        topRated = _topRated,
                        upcoming = _upcoming,
                        upcomingFromLibrary = currentState.upcomingFromLibrary,
                        recommended = currentState.recommended,
                        userStats = currentState.userStats
                    )
                }
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
