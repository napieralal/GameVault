package com.example.gamevault.ui.screens.games.GamesLibrary

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.navigation.NavHostController
import com.example.gamevault.GameVaultDestinations
import com.example.gamevault.data.remote.FirebaseLibraryService
import com.example.gamevault.data.repository.LibraryRepository
import com.example.gamevault.data.local.UserGameDao
import com.example.gamevault.ui.components.UserGameCard
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    navController: NavHostController,
    dao: UserGameDao,
    firebaseService: FirebaseLibraryService,
    libraryViewModel: LibraryViewModel = viewModel(factory = GameVaultAppViewModelProvider.Factory)
) {
    val userGames by libraryViewModel.userGames.collectAsState()
    val selectedStatus by libraryViewModel.selectedStatus.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filteredGames = if (selectedStatus == GameStatus.UNSPECIFIED) {
        userGames
    } else {
        userGames.filter { it.status == selectedStatus.name }
    }

    LaunchedEffect(true) {
        libraryViewModel.loadGames()
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                scope.launch {
                    LibraryRepository(dao, firebaseService).clearLocalData()
                    Toast.makeText(context, "Local data deleted", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Delete local data")
            }

            IconButton(onClick = {
                libraryViewModel.loadGames()
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync with cloud")
            }
        }

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
                        },
                        onGameClick = { clickedGame ->
                            navController.navigate("${GameVaultDestinations.GAME_DETAILS.name}/${clickedGame.gameId}")
                        },
                        onStatusChange = { updatedGame ->
                            libraryViewModel.updateGameStatus(updatedGame)
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

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        statuses.forEachIndexed { index, status ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = statuses.size
                ),
                onClick = { onSelect(status) },
                selected = selected == status,
                modifier = Modifier.weight(1f),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = status.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercaseChar() },
                    fontSize = 11.sp
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