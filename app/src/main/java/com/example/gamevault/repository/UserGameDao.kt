package com.example.gamevault.repository

import androidx.room.*
import com.example.gamevault.model.UserGameEntity

@Dao
interface UserGameDao {
    @Query("SELECT * FROM user_games")
    suspend fun getAllGames(): List<UserGameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: UserGameEntity)

    @Delete
    suspend fun deleteGame(game: UserGameEntity)

    @Query("DELETE FROM user_games WHERE gameId = :id")
    suspend fun deleteGameById(id: Long)

    @Query("DELETE FROM user_games")
    suspend fun clearAll()
}