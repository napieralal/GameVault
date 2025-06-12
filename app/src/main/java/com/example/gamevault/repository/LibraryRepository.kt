package com.example.gamevault.repository

import com.example.gamevault.model.UserGameEntity

class LibraryRepository(private val dao: UserGameDao) {
    suspend fun addGame(game: UserGameEntity) = dao.insertGame(game)
    suspend fun getAllGames() = dao.getAllGames()
    suspend fun deleteGameById(id: Long) = dao.deleteGameById(id)
}