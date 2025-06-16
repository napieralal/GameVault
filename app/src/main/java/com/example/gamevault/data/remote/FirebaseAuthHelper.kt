package com.example.gamevault.data.remote

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

object FirebaseAuthHelper {
    private val auth = FirebaseAuth.getInstance()

    val currentUser
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            val auth = FirebaseAuth.getInstance()
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.sendEmailVerification()?.await()
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            Firebase.auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
}