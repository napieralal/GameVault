package com.example.gamevault.ui.screens.games.GameDetails

import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Date
import java.util.Locale

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
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 8.dp)
                    ) {
                        GameTab.values().forEach { tab ->
                            Text(
                                text = tab.label,
                                color = if (tab == selectedTab)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurface,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                game.name ?: "Unknown",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold
            )

            // Metadane gry (data wydania, developer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                game.first_release_date?.let { timestamp ->
                    val date = Date(timestamp * 1000)
                    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val formattedDate = formatter.format(date)

                    InfoChip(
                        icon = Icons.Default.DateRange,
                        text = formattedDate
                    )
                }

                game.involved_companies
                    ?.firstOrNull { it.developer == true }
                    ?.company?.name
                    ?.let {
                        InfoChip(
                            icon = Icons.Default.DeveloperMode,
                            text = it
                        )
                    }
            }

            // Okładka i ocena
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                game.cover?.imageId?.let { imageId ->
                    val imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/$imageId.jpg"
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .width(140.dp)
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                RatingBox(
                    rating = game.total_rating,
                    ratingCount = game.rating_count
                )
            }
        }
    }
}

@Composable
private fun RatingBox(rating: Double?, ratingCount: Int?) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = rating?.let { "★ %.1f".format(it) } ?: "★ --",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = ratingCount?.let {
                    when {
                        it >= 1_000_000 -> "${it / 1_000_000}M+ ratings"
                        it >= 1_000 -> "${it / 1_000}k+ ratings"
                        else -> "$it ratings"
                    }
                } ?: "No ratings",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GameInfoSection(game: GameDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Genres
            if (!game.genres.isNullOrEmpty()) {
                LabeledInfo(
                    label = "Genres",
                    content = game.genres.joinToString { it.name }
                )
            }

            // Platforms
            if (!game.platforms.isNullOrEmpty()) {
                LabeledInfo(
                    label = "Platforms",
                    content = game.platforms.joinToString { it.name }
                )
            }

            // Summary
            game.summary?.let {
                ExpandableSection(
                    title = "Summary",
                    content = it
                )
            }
        }
    }
}

@Composable
private fun LabeledInfo(label: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun GameDetailsSection(game: GameDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Dzielimy sekcję na dwie kolumny
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lewa kolumna
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!game.involved_companies.isNullOrEmpty()) {
                        val developers = game.involved_companies
                            .filter { it.developer == true }
                            .map { it.company.name }
                        if (developers.isNotEmpty()) {
                            LabeledInfo(
                                label = "Developers",
                                content = developers.joinToString()
                            )
                        }

                        LabeledInfo(
                            label = "Involved Companies",
                            content = game.involved_companies.joinToString { it.company.name }
                        )
                    }

                    if (!game.game_engines.isNullOrEmpty()) {
                        LabeledInfo(
                            label = "Game Engine",
                            content = game.game_engines.joinToString { it.name }
                        )
                    }

                    if (!game.collections.isNullOrEmpty()) {
                        LabeledInfo(
                            label = "Series",
                            content = game.collections.joinToString { it.name ?: "Unknown" }
                        )
                    }
                }

                // Prawa kolumna
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!game.game_modes.isNullOrEmpty()) {
                        LabeledInfo(
                            label = "Game Modes",
                            content = game.game_modes.joinToString { it.name }
                        )
                    }

                    if (!game.player_perspectives.isNullOrEmpty()) {
                        LabeledInfo(
                            label = "Perspectives",
                            content = game.player_perspectives.joinToString { it.name }
                        )
                    }

                    if (!game.themes.isNullOrEmpty()) {
                        LabeledInfo(
                            label = "Themes",
                            content = game.themes.joinToString { it.name }
                        )
                    }
                }
            }

            // Storyline (pełna szerokość)
            game.storyline?.let {
                Spacer(modifier = Modifier.height(16.dp))
                ExpandableSection(
                    title = "Storyline",
                    content = it
                )
            }
        }
    }
}

@Composable
private fun ExpandableSection(title: String, content: String) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Show less" else "Show more",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Screenshots",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.heightIn(max = 800.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(screenshots) { screenshot ->
                    val url = "https://images.igdb.com/igdb/image/upload/t_screenshot_big/${screenshot.imageId}.jpg"
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clickable { expandedImageId = screenshot.imageId }
                        )
                    }
                }
            }
        }
    }

    expandedImageId?.let { imageId ->
        FullscreenImageDialog(
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_screenshot_huge/$imageId.jpg",
            onDismiss = { expandedImageId = null }
        )
    }
}





