package com.example.gamevault.ui.screens.games.GamesLibrary

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.model.GameStatus
import com.example.gamevault.model.UserGameEntity
import com.example.gamevault.data.remote.FirebaseLibraryService
import com.example.gamevault.data.local.GameVaultDatabase
import com.example.gamevault.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LibraryRepository
    private val _userGames = MutableStateFlow<List<UserGameEntity>>(emptyList())

    private val _selectedStatus = MutableStateFlow(GameStatus.UNSPECIFIED)
    val selectedStatus: StateFlow<GameStatus> = _selectedStatus
    val userGames: StateFlow<List<UserGameEntity>> = _userGames

    init {
        val db = GameVaultDatabase.getDatabase(application)
        val firebaseService = FirebaseLibraryService()
        repository = LibraryRepository(db.userGameDao(), firebaseService)

        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _userGames.value = repository.getAllGames()
        }
    }

    fun addGame(game: UserGameEntity) {
        viewModelScope.launch {
            repository.addGame(game)
            loadGames()
        }
    }

    fun deleteGame(id: Long) {
        viewModelScope.launch {
            repository.deleteGameById(id)
            loadGames()
        }
    }

    fun setFilter(status: GameStatus) {
        _selectedStatus.value = status
    }

    /*fun toggleStatus(game: UserGameEntity) {
        val nextStatus = GameStatus.values()
            .let { statuses ->
                val currentIndex = statuses.indexOfFirst { it.name == game.status }
                statuses[(currentIndex + 1) % statuses.size]
            }
        updateGameStatus(game.gameId, nextStatus.name)
    }*/

    fun updateGameStatus(updatedGame: UserGameEntity) {
        viewModelScope.launch {
            repository.updateGameStatus(updatedGame)
            loadGames()
        }
    }
}