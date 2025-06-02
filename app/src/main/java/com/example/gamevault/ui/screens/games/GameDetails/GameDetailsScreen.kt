package com.example.gamevault.ui.screens.games.GameDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.gamevault.GameVaultAppViewModelProvider
import com.example.gamevault.model.Game
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import com.example.gamevault.GameVaultDestinations
import com.example.gamevault.model.GameDetails
import com.example.gamevault.model.Screenshot
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.gamevault.ui.components.ExpandableText
import com.example.gamevault.ui.components.GameCardVertical
import com.example.gamevault.ui.components.SectionLabel
import com.example.gamevault.ui.components.FullscreenImageDialog
import androidx.compose.foundation.lazy.grid.items

enum class GameTab(val label: String) {
    ABOUT("About"),
    MEDIA("Media"),
    SIMILAR("Similar Games")
}

@Composable
fun GameDetailsScreen(
    navController: NavHostController,
    viewModel: GameDetailsViewModel = viewModel(factory = GameVaultAppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val gameIdTrigger = (uiState as? GameDetailsUiState.Success)?.game?.id

    LaunchedEffect(gameIdTrigger) {
        listState.scrollToItem(0)
    }

    when (val state = uiState) {
        is GameDetailsUiState.Initial,
        is GameDetailsUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is GameDetailsUiState.Error -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = (uiState as GameDetailsUiState.Error).message)
            }
        }

        is GameDetailsUiState.Success -> {
            var selectedTab by remember { mutableStateOf(GameTab.ABOUT) }
            val game = (uiState as GameDetailsUiState.Success).game
            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GameHeader(game)
                }

                item {
                    GameInfoSection(game)
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E))
                            .padding(vertical = 8.dp)
                    ) {
                        GameTab.values().forEach { tab ->
                            Text(
                                text = tab.label,
                                color = if (tab == selectedTab) Color.White else Color.Gray,
                                modifier = Modifier
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Normal)
                            )
                        }
                    }
                }

                when (selectedTab) {
                    GameTab.ABOUT -> {
                        item {
                            GameDetailsSection(game)
                        }
                    }

                    GameTab.MEDIA -> {
                        game.screenshots?.let {
                            item {
                                ScreenshotRow(screenshots = it)
                            }
                        }
                    }

                    GameTab.SIMILAR -> {
                        if (!game.similar_games.isNullOrEmpty()) {
                            item {
                                Text("Similar Games", style = MaterialTheme.typography.titleMedium)
                                SimilarGamesList(games = game.similar_games, navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameHeader(game: GameDetails) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            game.name ?: "Unknown",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            game.release_dates?.firstOrNull()?.human?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            game.involved_companies
                ?.firstOrNull { it.developer == true }
                ?.company?.name
                ?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            game.cover?.imageId?.let { imageId ->
                val imageUrl =
                    "https://images.igdb.com/igdb/image/upload/t_cover_big/$imageId.jpg"
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .width(120.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = game.total_rating?.let { "★ %.1f".format(it) } ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )

                    Text(
                        text = game.rating_count?.let {
                            val short = when {
                                it >= 1_000_000 -> "${it / 1_000_000}M+"
                                it >= 1_000 -> "${it / 1_000}k+"
                                else -> "$it"
                            }
                            "$short ratings"
                        } ?: "No rating",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}


@Composable
fun GameInfoSection(game: GameDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2E2E2E))
            .padding(16.dp)
    ) {
        game.genres?.joinToString { it.name }?.let {
            Text("Genres: $it", color = Color.White)
        }

        game.platforms?.joinToString { it.name }?.let {
            Text("Platforms: $it", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        game.summary?.let {
            ExpandableText(
                title = "Summary:",
                text = game.summary,
                textColor = Color.White,
                titleColor = Color.White,
                toggleColor = Color.LightGray
            )
        }
        /* game.franchises?.let {
            Text("Franchise: \n$it")
        }*/
    }
}

@Composable
fun GameDetailsSection(game: GameDetails) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // === Left Column ===
        Column(
            modifier = Modifier.weight(1f)
        ) {
            game.involved_companies
                ?.filter { it.developer == true }
                ?.map { it.company.name }
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString()
                ?.let {
                    SectionLabel("Main Developers")
                    Text(it)
                    Spacer(modifier = Modifier.height(12.dp))
                }

            game.involved_companies
                ?.map { it.company.name }
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString()
                ?.let {
                    SectionLabel("Involved Companies")
                    Text(it)
                    Spacer(modifier = Modifier.height(12.dp))
                }

            game.game_engines?.joinToString { it.name }?.let {
                SectionLabel("Game Engine")
                Text(it)
                Spacer(modifier = Modifier.height(12.dp))
            }

            game.collections?.joinToString { it.name ?: "Unknown" }?.let {
                SectionLabel("Series")
                Text(it)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // === Right Column ===
        Column(
            modifier = Modifier.weight(1f)
        ) {
            game.game_modes?.joinToString { it.name }?.let {
                SectionLabel("Game Modes")
                Text(it)
                Spacer(modifier = Modifier.height(12.dp))
            }

            game.player_perspectives?.joinToString { it.name }?.let {
                SectionLabel("Player Perspectives")
                Text(it)
                Spacer(modifier = Modifier.height(12.dp))
            }

            game.themes?.joinToString { it.name }?.let {
                SectionLabel("Themes")
                Text(it)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // === Storyline (Full Width Underneath) ===
    game.storyline?.let {
        ExpandableText(
            title = "Storyline:",
            text = it,
            textColor = Color.Black,
            titleColor = Color.Black,
            toggleColor = Color.DarkGray
        )
    }
}

@Composable
fun SimilarGamesList(games: List<Game>, navController: NavHostController) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(games) { game ->
            GameCardVertical(game = game) {
                game.id.let { id ->
                    navController.navigate("${GameVaultDestinations.GAME_DETAILS.name}/${game.id}")
                }
            }
        }
    }
}

@Composable
fun ScreenshotRow(screenshots: List<Screenshot>) {
    var expandedImageId by remember { mutableStateOf<String?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 800.dp), // Możesz dostosować maksymalną wysokość
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(screenshots) { screenshot ->
            val url = "https://images.igdb.com/igdb/image/upload/t_screenshot_big/${screenshot.imageId}.jpg"
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .width(200.dp)
                    .height(112.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expandedImageId = screenshot.imageId }
            )
        }
    }

    // Fullscreen image dialog
    expandedImageId?.let { imageId ->
        FullscreenImageDialog(
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_screenshot_huge/$imageId.jpg",
            onDismiss = { expandedImageId = null }
        )
    }
}





