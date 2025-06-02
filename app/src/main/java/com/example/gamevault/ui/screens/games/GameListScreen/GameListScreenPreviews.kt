package com.example.gamevault.ui.screens.games.GameListScreen

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gamevault.model.Cover
import com.example.gamevault.model.Game
import com.example.gamevault.model.Genre
import com.example.gamevault.ui.components.GameCardHorizontal
import com.example.gamevault.ui.theme.GameVaultTheme

private val sampleGames = listOf(
    Game(
        id = 1,
        name = "Cyberpunk 2077",
        genres = listOf(Genre("RPG"), Genre("Action")),
        cover = Cover("co1wwy"),
        total_rating = 10.0
    ),
    Game(
        id = 2,
        name = "The Witcher 3: Wild Hunt",
        genres = listOf(Genre("RPG"), Genre("Adventure")),
        cover = Cover("co1tmu"),
        total_rating = 9.5
    ),
    Game(
        id = 3,
        name = "Elden Ring",
        genres = listOf(Genre("RPG"), Genre("Soulslike")),
        cover = Cover("co2gsc"),
        total_rating = 9.0
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GameListScreenPreview() {
    GameVaultTheme {
        LazyGameListScreenContent(
            games = sampleGames,
            onGameClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameCardPreview() {
    GameVaultTheme {
        GameCardHorizontal(
            game = sampleGames.first(),
            onClick = {}
        )
    }
}

@Composable
fun LazyGameListScreenContent(
    games: List<Game>,
    onGameClick: (Game) -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        items(games) { game ->
            GameCardHorizontal(
                game = game,
                onClick = { onGameClick(game) }
            )
        }
    }
}