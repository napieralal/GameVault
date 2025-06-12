package com.example.gamevault.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Cover(
    @Json(name = "image_id") val imageId: String
)

@JsonClass(generateAdapter = true)
data class Genre(val name: String)

@JsonClass(generateAdapter = true)
data class Platform(val name: String)

@JsonClass(generateAdapter = true)
data class Game(
    val id: Long,
    val name: String?,
    val genres: List<Genre>?,
    val cover: Cover?,
    val total_rating: Double?,
    val first_release_date: Long?,
    val platforms: List<Platform>?,
    val game_type: Int?,
    val release_dates: List<ReleaseDate>?
    /*@Json(name = "rating_count") val ratingCount: Int?,
    @Json(name = "aggregated_rating") val aggregatedRating: Double?,*/
)
    /*
    val total_rating: Double?,
    val slug: String?, //?
    val game_type: Int?,
    val url: String?, //?
    val summary: String?,
    val storyline: String?,

    @Json(name = "created_at") val createdAt: Long?,
    @Json(name = "updated_at") val updatedAt: Long?,
    @Json(name = "first_release_date") val firstReleaseDate: Long?,
    @Json(name = "age_ratings") val ageRatings: List<Long>?,
    @Json(name = "aggregated_rating_count") val aggregatedRatingCount: Int?,
    @Json(name = "aggregated_rating") val aggregatedRating: Double?,

    @Json(name = "alternative_names") val alternativeNames: List<Long>?, //?
    val artworks: List<Long>?,
    val bundles: List<Long>?, //?
    val dlcs: List<Long>?,
    val expansions: List<Long>?,
    @Json(name = "external_games") val externalGames: List<Long>?,
    val franchises: List<Long>?,
    @Json(name = "game_engines") val gameEngines: List<Long>?,
    @Json(name = "game_modes") val gameModes: List<Int>?,

    val hypes: Int?,
    @Json(name = "involved_companies") val involvedCompanies: List<Long>?,
    val keywords: List<Long>?,
    val platforms: List<Int>?,
    @Json(name = "player_perspectives") val playerPerspectives: List<Int>?,
    @Json(name = "rating") val rating: Double?,
    @Json(name = "rating_count") val ratingCount: Int?,
    @Json(name = "release_dates") val releaseDates: List<Long>?, //?
    val screenshots: List<Long>?,
    @Json(name = "similar_games") val similarGames: List<Long>?,
    val themes: List<Int>?,
    val tags: List<Long>?,

    @Json(name = "total_rating_count") val totalRatingCount: Int?,
    val videos: List<Long>?,
    val websites: List<Long>?,
    @Json(name = "language_supports") val languageSupports: List<Long>?,
    @Json(name = "game_localizations") val gameLocalizations: List<Long>?,
    val collections: List<Long>?,
    val checksum: String?,
    @Json(name = "game_type") val gameType: Int?
)*/

