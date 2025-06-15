package com.example.gamevault.model


import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

enum class GameStatus {
    UNSPECIFIED,
    WANT_TO_PLAY,
    PLAYING,
    COMPLETED
}

@Keep
@IgnoreExtraProperties
@Entity(tableName = "user_games")
data class UserGameEntity(
    @PrimaryKey val gameId: Long = 0L,
    val name: String = "",
    val coverUrl: String? = null,
    val status: String = ""
)
