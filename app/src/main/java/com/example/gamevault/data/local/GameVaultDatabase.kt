package com.example.gamevault.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gamevault.model.UserGameEntity
import com.example.gamevault.data.local.UserGameDao

@Database(entities = [UserGameEntity::class], version = 1)
abstract class GameVaultDatabase : RoomDatabase() {
    abstract fun userGameDao(): UserGameDao

    companion object {
        @Volatile
        private var INSTANCE: GameVaultDatabase? = null

        fun getDatabase(context: Context): GameVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    GameVaultDatabase::class.java,
                    "game_vault_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}