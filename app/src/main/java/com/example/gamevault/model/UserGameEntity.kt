package com.example.gamevault.model


import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GameStatus {
    UNSPECIFIED,
    WANT_TO_PLAY,
    PLAYING,
    COMPLETED
}

@Entity(tableName = "user_games")
data class UserGameEntity(
    @PrimaryKey val gameId: Long,
    val name: String,
    val coverUrl: String?,
    val status: String // "Want to play", "Playing", etc.
)

fun UserGameEntity.toGame(): Game {
    return Game(
        id = this.gameId,
        name = this.name,
        genres = null,  // brak info, można dodać pustą listę lub null
        cover = this.coverUrl?.let { Cover(imageId = it) }, // jeśli Cover wymaga imageId, ale masz url? może trzeba zmienić model
        total_rating = null,
        first_release_date = null,
        platforms = null,
        game_type = null,
        release_dates = null
    )
}