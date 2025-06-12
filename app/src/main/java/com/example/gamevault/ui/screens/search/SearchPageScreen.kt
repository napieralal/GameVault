package com.example.gamevault.ui.screens.search

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.gamevault.GameVaultAppViewModelProvider
import com.example.gamevault.GameVaultDestinations
import com.example.gamevault.ui.components.GameCardHorizontal
import com.example.gamevault.ui.components.SortMenu
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Icon
import com.example.gamevault.ui.components.FilterBottomSheetContent

private val genreNameToId = mapOf(
    "Pinball" to 30,
    "Adventure" to 31,
    "Indie" to 32,
    "Arcade" to 33,
    "Visual Novel" to 34,
    "Card & Board Game" to 35,
    "MOBA" to 36,
    "Point-and-click" to 2,
    "Fighting" to 4,
    "Shooter" to 5,
    "Music" to 7,
    "Platform" to 8,
    "Puzzle" to 9,
    "Racing" to 10,
    "Real Time Strategy (RTS)" to 11,
    "Role-playing (RPG)" to 12,
    "Simulator" to 13,
    "Sport" to 14,
    "Strategy" to 15,
    "Turn-based strategy (TBS)" to 16,
    "Tactical" to 17,
    "Hack and slash/Beat 'em up" to 18,
    "Quiz/Trivia" to 26
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = viewModel(factory = GameVaultAppViewModelProvider.Factory),
    genreFilter: String?,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(genreFilter) {
        genreFilter?.let { genreName ->
            val matchingGenreId = genreNameToId[genreName]
            matchingGenreId?.let { id ->
                viewModel.updateFilters(
                    viewModel.filters.value.copy(
                        selectedGenreIds = (viewModel.filters.value.selectedGenreIds + id).distinct()
                    )
                )
            }
        }
    }

    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    var query by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val filters by viewModel.filters.collectAsState()
    val endReached by viewModel.endReached.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pendingFilters by remember { mutableStateOf<SearchFilters?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchBarWithFilterIcon(
            query = query,
            onQueryChange = {
                query = it
                viewModel.updateQuery(it)
            },
            onFilterClick = {
                pendingFilters = filters.copy()
                showFilters = true
            }
        )

        SortBar(
            currentSort = filters.sortType,
            onSortSelected = viewModel::updateSort
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (showFilters && pendingFilters != null) {
            ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                sheetState = bottomSheetState
            ) {
                FilterBottomSheetContent(
                    viewModel = viewModel,
                    pendingFilters = pendingFilters!!,
                    onFiltersChange = { pendingFilters = it },
                    onApply = {
                        viewModel.updateFilters(it)
                        showFilters = false
                    },
                    onDismiss = { showFilters = false }
                )
            }
        }

        when (val state = uiState) {
            is SearchUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is SearchUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            is SearchUiState.Success -> {
                val games = state.games

                if (games.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No results found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(games) { game ->
                            GameCardHorizontal(game = game) {
                                navController.navigate("${GameVaultDestinations.GAME_DETAILS.name}/${game.id}")
                            }
                        }

                        if (isLoadingMore && !endReached) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        } else if (endReached) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No more results.",
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo }
                        .collect { layoutInfo ->
                            val totalItems = layoutInfo.totalItemsCount
                            val lastVisible =
                                layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                            if (lastVisible >= totalItems - 3) {
                                viewModel.loadMore()
                            }
                        }
                }
            }

            SearchUiState.Initial -> {
                Text(
                    "Start typing to search games...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SearchBarWithFilterIcon(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    "Search games...",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Outlined.FilterList,
            contentDescription = "Filter",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clickable(onClick = onFilterClick)
                .padding(8.dp)
                .size(24.dp)
        )
    }
}

@Composable
fun SortBar(
    currentSort: SortType,
    onSortSelected: (SortType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        SortMenu(
            currentSort = currentSort,
            onSortSelected = onSortSelected
        )
    }
}





