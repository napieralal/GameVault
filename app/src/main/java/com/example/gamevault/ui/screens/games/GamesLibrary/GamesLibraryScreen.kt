package com.example.gamevault.ui.screens.games.GamesLibrary

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.gamevault.GameVaultAppViewModelProvider
import com.example.gamevault.model.GameStatus
import com.example.gamevault.model.UserGameEntity
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = viewModel(factory = GameVaultAppViewModelProvider.Factory)
) {
    val userGames by libraryViewModel.userGames.collectAsState()
    val selectedStatus by libraryViewModel.selectedStatus.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filteredGames = if (selectedStatus == GameStatus.UNSPECIFIED) {
        userGames
    } else {
        userGames.filter { it.status == selectedStatus.name }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        StatusSelector(
            selected = selectedStatus,
            onSelect = libraryViewModel::setFilter
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredGames.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No games here", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredGames) { game ->
                    UserGameCard(
                        entry = game,
                        onDelete = {
                            libraryViewModel.deleteGame(game.gameId)
                            scope.launch {
                                snackbarHostState.showSnackbar("Game deleted")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusSelector(selected: GameStatus, onSelect: (GameStatus) -> Unit) {
    val statuses = GameStatus.values()

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statuses.forEach { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelect(status) },
                label = {
                    Text(
                        status.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercaseChar() }
                    )
                }
            )
        }
    }
}

@Composable
fun UserGameCard(
    entry: UserGameEntity,
    onDelete: () -> Unit // Dodajemy callback do usuwania
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) } // Stan dla dialogu

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            gameName = entry.name,
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                onDelete()
                showDeleteConfirmation = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(entry.coverUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.status.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercaseChar() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Przycisk kosza
            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete game",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { showDeleteConfirmation = true },
                        role = Role.Button,
                        indication = ripple()
                    )
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    gameName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete game?")
        },
        text = {
            Text("Are you sure you want to delete \"$gameName\" from your library?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}