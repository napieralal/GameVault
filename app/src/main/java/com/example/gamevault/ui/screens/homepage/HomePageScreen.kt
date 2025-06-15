package com.example.gamevault.ui.screens.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.gamevault.GameVaultAppViewModelProvider
import com.example.gamevault.GameVaultDestinations
import com.example.gamevault.model.Game
import com.example.gamevault.ui.components.GameCardVertical
import java.time.LocalDate

@Composable
fun HomePageScreen(
    navController: NavHostController,
    viewModel: HomePageViewModel = viewModel(factory = GameVaultAppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()

    when (uiState) {
        is HomeUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is HomeUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = (uiState as HomeUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        is HomeUiState.Success -> {
            val state = uiState as HomeUiState.Success

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier
                    .padding(8.dp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                item {
                    UserLibrarySection(
                        upcomingFromLibrary = state.upcomingFromLibrary,
                        recommendedGames = state.recommended,
                        userStats = state.userStats,
                        navController = navController
                    )
                }
                item {
                    HorizontalGameList(
                        title = "🔥 Popular Games",
                        games = state.popular,
                        isLoadingMore = isLoadingMore,
                        onLoadMore = { viewModel.loadMore(GameSection.POPULAR) },
                        onGameClick = { game ->
                            navController.navigate("${GameVaultDestinations.GAME_DETAILS.name}/${game.id}")
                        }
                    )
                }

                item {
                    HorizontalGameList(
                        title = "🆕 New Releases",
                        games = state.newReleases,
                        isLoadingMore = isLoadingMore,
                        onLoadMore = { viewModel.loadMore(GameSection.NEW_RELEASES) },
                        onGameClick = { game ->
                            navController.navigate("${GameVaultDestinations.GAME_DETAILS.name}/${game.id}")
                        }
                    )
                }

                item {
                    HorizontalGameList(
                        title = "🔜 Upcoming Games",
                        games = state.upcoming,
                        isLoadingMore = isLoadingMore,
                        onLoadMore = { viewModel.loadMore(GameSection.UPCOMING) },
                        onGameClick = { game ->
                            navController.navigate("${GameVaultDestinations.GAME_DETAILS.name}/${game.id}")
                        }
                    )
                }

                item {
                    HorizontalGameList(
                        title = "⭐ Top Rated",
                        games = state.topRated,
                        isLoadingMore = isLoadingMore,
                        onLoadMore = { viewModel.loadMore(GameSection.TOP_RATED) },
                        onGameClick = { game ->
                            navController.navigate("${GameVaultDestinations.GAME_DETAILS.name}/${game.id}")
                        })
                }

                // Opcjonalna sekcja z gatunkami
                /*
                item {
                    GenreChips(
                        genres = state.genreChips,
                        onGenreClick = { selected ->
                            navController.navigate("${GameVaultDestinations.SEARCH.name}?genreFilter=${selected.genreName}")
                        }
                    )
                }
                */
            }
        }
    }
}

@Composable
fun UserLibrarySection(
    upcomingFromLibrary: List<Game>,
    recommendedGames: List<Game>,
    userStats: UserStats?,
    navController: NavHostController
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (upcomingFromLibrary.isNotEmpty()) {
            HorizontalGameList(
                title = "📅 Upcoming From Your Library",
                games = upcomingFromLibrary,
                isLoadingMore = false,
                onLoadMore = {},
                onGameClick = { game ->
                    navController.navigate("${GameVaultDestinations.GAME_DETAILS.name}/${game.id}")
                }
            )
        }

        if (recommendedGames.isNotEmpty()) {
            HorizontalGameList(
                title = "🎯 Recommended For You",
                games = recommendedGames,
                isLoadingMore = false,
                onLoadMore = {},
                onGameClick = { game ->
                    navController.navigate("${GameVaultDestinations.GAME_DETAILS.name}/${game.id}")
                }
            )
        }

        userStats?.let { stats ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Your Gaming Stats",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))

                    Text("• Own games: ${stats.total}")
                    Text("• Want to Play: ${stats.wantToPlay}")
                    Text("• Playing: ${stats.playing}")
                    Text("• Completed: ${stats.completed}")

                    val favoriteGenre = stats.genreCounts.maxByOrNull { it.value }?.key ?: "Unknown"
                    Text("• Favourite genre: $favoriteGenre")
                }
            }
        }
    }
}


@Composable
fun HorizontalGameList(
    title: String,
    games: List<Game>,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onGameClick: (Game) -> Unit
) {
    val lazyRowState = rememberLazyListState()

    LaunchedEffect(lazyRowState) {
        snapshotFlow { lazyRowState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty() && !isLoadingMore) {
                    if (visibleItems.last().index >= games.size - 3) {
                        onLoadMore()
                    }
                }
            }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(horizontal = 0.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                state = lazyRowState,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(games) { game ->
                    GameCardVertical(
                        game = game,
                        modifier = Modifier.width(150.dp),
                        onClick = { onGameClick(game) }
                    )
                }

                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
