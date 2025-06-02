package com.example.gamevault.ui.screens.search

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = viewModel(factory = GameVaultAppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.updateQuery(it)
                },
                placeholder = { Text("Search games...") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Advanced Search",
                color = Color.Black,
                modifier = Modifier
                    .clickable {
                        pendingFilters = viewModel.filters.value.copy()
                        showFilters = true
                    }
                    .padding(8.dp)
            )
        }


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

        /*if (showFilters) {
            Spacer(modifier = Modifier.height(8.dp))

            val sections = viewModel.getFilterSections()

            Column {
                sections.forEach { section ->
                    Text(section.title)

                    when (section.type) {
                        FilterType.CHECKBOX -> {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                section.items.forEach { item ->
                                    FilterChip(
                                        selected = section.selectedIds.contains(item.id),
                                        onClick = { section.onToggle?.invoke(item.id) },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }

                        FilterType.RANGE -> {
                            val selectedRange = section.selectedRange ?: section.range
                            RangeSlider(
                                value = selectedRange!!.start.toFloat()..selectedRange.endInclusive.toFloat(),
                                onValueChange = {
                                    section.onRangeChange?.invoke(it.start.toInt()..it.endInclusive.toInt())
                                },
                                valueRange = section.range!!.start.toFloat()..section.range.endInclusive.toFloat(),
                                steps = 10
                            )
                            Text("${selectedRange.start} - ${selectedRange.endInclusive}")
                        }

                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }*/

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            SortMenu(
                currentSort = filters.sortType,
                onSortSelected = viewModel::updateSort
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is SearchUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is SearchUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red)
                }
            }

            is SearchUiState.Success -> {
                val games = state.games

                if (games.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No results found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
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
                                    CircularProgressIndicator()
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
                                    Text("No more results.", color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo }
                        .collect { layoutInfo ->
                            val totalItems = layoutInfo.totalItemsCount
                            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                            if (lastVisible >= totalItems - 3) {
                                viewModel.loadMore()
                            }
                        }
                }
            }

            SearchUiState.Initial -> {
                Text(
                    "Start typing to search games...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun FilterBottomSheetContent(
    viewModel: SearchViewModel,
    pendingFilters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    onApply: (SearchFilters) -> Unit,
    onDismiss: () -> Unit
) {
    val sections = viewModel.getFilterSections(pendingFilters, onFiltersChange)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Advanced Filters", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        sections.forEach { section ->
            Text(section.title, style = MaterialTheme.typography.bodyMedium)

            when (section.type) {
                FilterType.CHECKBOX -> {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        section.items.forEach { item ->
                            FilterChip(
                                selected = section.selectedIds.contains(item.id),
                                onClick = { section.onToggle?.invoke(item.id) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }

                FilterType.RANGE -> {
                    val selectedRange = section.selectedRange ?: section.range
                    RangeSlider(
                        value = selectedRange!!.start.toFloat()..selectedRange.endInclusive.toFloat(),
                        onValueChange = {
                            section.onRangeChange?.invoke(it.start.toInt()..it.endInclusive.toInt())
                        },
                        valueRange = section.range!!.start.toFloat()..section.range.endInclusive.toFloat(),
                        steps = 10
                    )
                    Text("${selectedRange.start} - ${selectedRange.endInclusive}")
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Apply",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable {
                        onApply(pendingFilters)
                    }
                    .padding(8.dp)
            )
        }
    }
}



