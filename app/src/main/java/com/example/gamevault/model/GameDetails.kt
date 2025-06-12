package com.example.gamevault.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameMode(val name: String)

@JsonClass(generateAdapter = true)
data class PlayerPerspective(val name: String)

@JsonClass(generateAdapter = true)
data class Theme(val name: String)

@JsonClass(generateAdapter = true)
data class Collections(val name: String)

@JsonClass(generateAdapter = true)
data class Franchises(val name: String)

@JsonClass(generateAdapter = true)
data class GameEngine(val name: String)

@JsonClass(generateAdapter = true)
data class Company(val name: String)

@JsonClass(generateAdapter = true)
data class InvolvedCompany(
    val company: Company,
    val developer: Boolean?
)

@JsonClass(generateAdapter = true)
data class Screenshot(
    @Json(name = "image_id") val imageId: String
)

@JsonClass(generateAdapter = true)
data class ReleaseDate(
    val human: String?
)

@JsonClass(generateAdapter = true)
data class GameDetails(
    val id: Long,
    val name: String?,
    val summary: String?,
    val storyline: String?,
    val release_dates: List<ReleaseDate>?,
    val genres: List<Genre>?,
    val cover: Cover?,
    val total_rating: Double?,
    val rating_count: Int?,
    val aggregated_rating: Double?,
    val screenshots: List<Screenshot>?,
    val platforms: List<Platform>?,
    val involved_companies: List<InvolvedCompany>?,
    val game_modes: List<GameMode>?,
    val player_perspectives: List<PlayerPerspective>?,
    val themes: List<Theme>?,
    //val franchises: Franchises?,
    val collections: List<Collections>?,
    val game_engines: List<GameEngine>?,
    val first_release_date: Long?,
    val similar_games: List<Game>?,
)