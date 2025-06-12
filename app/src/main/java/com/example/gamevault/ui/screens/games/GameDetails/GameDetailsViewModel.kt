package com.example.gamevault.ui.screens.games.GameDetails

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.R
import com.example.gamevault.model.GameDetails
import com.example.gamevault.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

sealed interface GameDetailsUiState {
    object Initial : GameDetailsUiState
    object Loading : GameDetailsUiState
    data class Success(val game: GameDetails) : GameDetailsUiState
    data class Error(@StringRes val titleRes: Int, val message: String) : GameDetailsUiState
}

class GameDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val gameDetailsApiService: ApiService
) : ViewModel() {
    private val gameId: Long = checkNotNull(savedStateHandle["gameId"])

    private val _uiState = MutableStateFlow<GameDetailsUiState>(
        GameDetailsUiState.Initial)
    val uiState: StateFlow<GameDetailsUiState> = _uiState.asStateFlow()

    init {
        fetchGameDetails()
    }

    private fun fetchGameDetails() {
        _uiState.value = GameDetailsUiState.Loading
        val query = """
        fields id,name,summary,storyline,genres.name,platforms.name,first_release_date,
        release_dates.human,involved_companies.company.name,involved_companies.developer,
        screenshots.image_id,cover.image_id,total_rating,rating_count,
        themes.name,game_modes.name,player_perspectives.name,
        game_engines.name,
        similar_games.name,
        similar_games.cover.image_id,
        similar_games.genres.name,
        similar_games.total_rating,
        collections.name;
        where id = $gameId;
        """.trimIndent().toRequestBody("text/plain".toMediaType())

        viewModelScope.launch {
            try {
                val result = gameDetailsApiService.getGameDetails(query)
                val game = result.firstOrNull()
                if (game != null) {
                    _uiState.value = GameDetailsUiState.Success(game)
                    Log.d("GameDetails", "Fetched game: $game")
                } else {
                    _uiState.value = GameDetailsUiState.Error(R.string.error, "Brak danych o grze.")
                }
            } catch (e: IOException) {
                _uiState.value = GameDetailsUiState.Error(R.string.network_error, "Błąd sieci: ${e.message}")
            } catch (e: HttpException) {
                _uiState.value = GameDetailsUiState.Error(R.string.error, "Błąd API: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = GameDetailsUiState.Error(R.string.error, "Nieznany błąd: ${e.message}")
            }
        }
    }
}