package com.example.gamevault.ui.screens.games.GameListScreen

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.R
import com.example.gamevault.model.Game
import com.example.gamevault.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

sealed interface GameListUiState {
    object Initial : GameListUiState
    object Loading : GameListUiState
    data class Success(val games: List<Game>) : GameListUiState
    data class Error(@StringRes val titleRes: Int, val message: String) : GameListUiState
}

class GameListScreenViewModel (
    private val savedStateHandle: SavedStateHandle,
    private val gamesListApiService: ApiService
) : ViewModel() {
    //private val gameApiService = RetrofitClient.apiServiceInstance

    private val _uiState = MutableStateFlow<GameListUiState>(
        GameListUiState.Initial)
    val uiState: StateFlow<GameListUiState> = _uiState.asStateFlow()

    init {
        fetchGames()
    }

    fun fetchGames() {
        _uiState.value = GameListUiState.Loading

        //val query = "fields id,name,genres.name,cover.image_id, total_rating; limit 10;"
        val query = "fields id,name,genres.name,cover.image_id; where id = 1942;"
        val body = query.toRequestBody("text/plain".toMediaType())

        viewModelScope.launch {
            try {
                val games = gamesListApiService.getGames(body)
                _uiState.value = GameListUiState.Success(games)
            } catch (e: IOException) {
                _uiState.value = GameListUiState.Error(
                    titleRes = R.string.network_error,
                    message = "Network error: ${e.message ?: "Unknown error"}"
                )
            } catch (e: HttpException) {
                _uiState.value = GameListUiState.Error(
                    titleRes = R.string.error,
                    message = "API error: ${e.code()} ${e.message()}"
                )
            } catch (e: Exception) {
                _uiState.value = GameListUiState.Error(
                    titleRes = R.string.error,
                    message = "Error: ${e.message ?: "Unknown error"}"
                )
            }
        }

        /*gameApiService.getGames(body).enqueue(object : Callback<List<Game>> {
            override fun onResponse(call: Call<List<Game>>, response: Response<List<Game>>) {
                if (response.isSuccessful) {
                    val games = response.body() ?: emptyList()
                    _uiState.value = GameListUiState.Success(games)
                } else {
                    _uiState.value = GameListUiState.Error(
                        titleRes = R.string.error,
                        message = "Błąd API: ${response.code()} ${response.message()}"
                    )
                }
            }

            override fun onFailure(call: Call<List<Game>>, t: Throwable) {
                _uiState.value = GameListUiState.Error(
                    titleRes = R.string.network_error,
                    message = "Błąd sieci: ${t.message}"
                )
            }
        })*/
    }
}