package com.example.gamevault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gamevault.model.UserGameEntity

@Dao
interface UserGameDao {
    @Query("SELECT * FROM user_games")
    suspend fun getAllGames(): List<UserGameEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertGame(game: UserGameEntity)

    @Delete
    suspend fun deleteGame(game: UserGameEntity)

    @Query("DELETE FROM user_games WHERE gameId = :id")
    suspend fun deleteGameById(id: Long)

    @Query("DELETE FROM user_games")
    suspend fun clearAll()

    @Query("UPDATE user_games SET status = :newStatus WHERE gameId = :gameId")
    suspend fun updateGameStatus(gameId: Long, newStatus: String)
}