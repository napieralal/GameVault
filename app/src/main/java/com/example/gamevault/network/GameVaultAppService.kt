package com.example.gamevault.network

import com.example.gamevault.model.Game
import com.example.gamevault.model.GameDetails
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

const val clientId = "xmn1wndpubbcock9zo72awnb029akv"
const val auth = "Bearer zvy5ljtk5ac82seefns3ovycjba7nj"

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
        "Client-ID: xmn1wndpubbcock9zo72awnb029akv",
        "Authorization: Bearer zvy5ljtk5ac82seefns3ovycjba7nj",
        "Accept: application/json"
    )
    @POST("games")
    suspend fun getGameDetails(
        @Body body: RequestBody
    ): List<GameDetails>
}