package com.example.gamevault.ui.screens.games.GamesLibrary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.model.GameStatus
import com.example.gamevault.model.UserGameEntity
import com.example.gamevault.repository.GameVaultDatabase
import com.example.gamevault.repository.LibraryRepository
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
        val db = GameVaultDatabase.Companion.getDatabase(application)
        repository = LibraryRepository(db.userGameDao())
        loadGames()
    }

    private fun loadGames() {
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
}