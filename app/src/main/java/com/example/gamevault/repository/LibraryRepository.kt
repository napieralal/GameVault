package com.example.gamevault.repository

import android.util.Log
import com.example.gamevault.model.UserGameEntity
import com.example.gamevault.network.FirebaseLibraryService
import com.google.firebase.auth.FirebaseAuth

class LibraryRepository(
    private val dao: UserGameDao,
    private val firebaseService: FirebaseLibraryService
) {
    private val isLoggedIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null

    suspend fun addGame(game: UserGameEntity) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            firebaseService.addGameToCloud(game)
        } else {
            dao.insertGame(game)
        }
    }

    suspend fun getAllGames(): List<UserGameEntity> {
        return if (FirebaseAuth.getInstance().currentUser != null) {
            firebaseService.getGamesFromCloud()
        } else {
            dao.getAllGames()
        }
    }

    suspend fun getAllGameIds(): List<Long> {
        return if (FirebaseAuth.getInstance().currentUser != null) {
            firebaseService.getGamesFromCloud().map { it.gameId }
        } else {
            dao.getAllGames().map { it.gameId }
        }
    }

    suspend fun deleteGameById(id: Long) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            firebaseService.deleteGameFromCloud(id)
        } else {
            dao.deleteGameById(id)
        }
    }

    suspend fun syncLocalGamesToCloud() {
        if (!isLoggedIn) return

        val localGames = dao.getAllGames()
        if (localGames.isEmpty()) return

        localGames.forEach { game ->
            firebaseService.addGameToCloud(game)
        }

        //dao.clearAll()
    }

    suspend fun clearLocalData() {
        dao.clearAll()
    }
}