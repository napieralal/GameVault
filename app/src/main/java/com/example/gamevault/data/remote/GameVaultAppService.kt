package com.example.gamevault.data.remote

import com.example.gamevault.model.Game
import com.example.gamevault.model.GameDetails
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

const val clientId = "xxx"
const val auth = "Bearer xxx"

interface ApiService {
    @Headers(
        "Client-ID: $clientId",
        "Authorization: $auth",
        "Accept: application/json"
    )
    @POST("games")
    suspend fun getGames(
        @Body body: RequestBody
    ): List<Game>

    @Headers(
        "Client-ID: $clientId",
        "Authorization: $auth",
        "Accept: application/json"
    )
    @POST("games")
    suspend fun getGameDetails(
        @Body body: RequestBody
    ): List<GameDetails>
}
