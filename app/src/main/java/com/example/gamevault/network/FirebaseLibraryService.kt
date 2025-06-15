package com.example.gamevault.network

import android.util.Log
import com.example.gamevault.model.UserGameEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseLibraryService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun addGameToCloud(game: UserGameEntity) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("games").document(game.gameId.toString())
            .set(game, SetOptions.merge()).await()
    }

    suspend fun deleteGameFromCloud(gameId: Long) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("games").document(gameId.toString())
            .delete().await()
    }

    suspend fun getGamesFromCloud(): List<UserGameEntity> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("games").get().await()

            snapshot.documents.mapNotNull { it.toObject(UserGameEntity::class.java) }
        } catch (e: Exception) {
            Log.e("FirebaseLibraryService", "getGamesFromCloud failed", e)
            emptyList()
        }
    }
}