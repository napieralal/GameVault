package com.example.gamevault.ui.screens.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
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

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            state = lazyRowState,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
