package com.example.gamevault.ui.screens.games.GameDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gamevault.model.Collections
import com.example.gamevault.model.Company
import com.example.gamevault.model.Cover
import com.example.gamevault.model.Game
import com.example.gamevault.model.GameDetails
import com.example.gamevault.model.GameEngine
import com.example.gamevault.model.GameMode
import com.example.gamevault.model.Genre
import com.example.gamevault.model.InvolvedCompany
import com.example.gamevault.model.Platform
import com.example.gamevault.model.PlayerPerspective
import com.example.gamevault.model.ReleaseDate
import com.example.gamevault.model.Screenshot
import com.example.gamevault.model.Theme
import com.example.gamevault.ui.theme.GameVaultTheme
import androidx.compose.ui.res.painterResource

private val sampleGameDetails = GameDetails(
    id = 1,
    name = "Cyberpunk 2077",
    genres = listOf(Genre("RPG"), Genre("Action")),
    cover = Cover("local_cover"),
    total_rating = 9.1,
    rating_count = 12456,
    aggregated_rating = 8.5,
    summary = "Cyberpunk 2077 is an open-world, action-adventure story set in Night City...",
    storyline = "You play as V, a mercenary outlaw going after a one-of-a-kind implant...",
    release_dates = listOf(ReleaseDate("December 10, 2020")),
    platforms = listOf(Platform("PC"), Platform("PS5")),
    game_engines = listOf(GameEngine("REDengine 4")),
    game_modes = listOf(GameMode("Singleplayer")),
    player_perspectives = listOf(PlayerPerspective("First person")),
    involved_companies = listOf(
        InvolvedCompany(company = Company("CD Projekt"), developer = true)
    ),
    screenshots = listOf(Screenshot("sc1xyz"), Screenshot("sc2abc")),
    themes = listOf(Theme("Cyberpunk")),
    collections = listOf(Collections("Cyberpunk Series")),
    similar_games = listOf(
        Game(
        id = 3,
        name = "Elden Ring",
        genres = listOf(Genre("RPG"), Genre("Soulslike")),
            cover = Cover("local_elden_ring"),
        total_rating = 9.0
    ),
    Game(
        id = 3,
        name = "Elden Ring",
        genres = listOf(Genre("RPG"), Genre("Soulslike")),
        cover = Cover("local_elden_ring"),
        total_rating = 9.0
    )
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GameDetailsPreview() {
    GameVaultTheme {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GameHeader(sampleGameDetails)
            }

            item {
                GameInfoSection(sampleGameDetails)
            }

            item {
                ScreenshotRow(sampleGameDetails.screenshots ?: emptyList())
            }
        }
    }
}