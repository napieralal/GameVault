package com.example.gamevault.ui.components

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gamevault.model.Game
import java.time.Instant
import java.time.ZoneOffset
import androidx.compose.ui.res.painterResource
import com.example.gamevault.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import com.example.gamevault.ui.screens.homepage.GenreWithGameCover
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamevault.GameVaultAppViewModelProvider
import com.example.gamevault.model.GameStatus
import com.example.gamevault.model.UserGameEntity
import com.example.gamevault.ui.screens.games.GamesLibrary.LibraryViewModel
import androidx.compose.ui.platform.LocalContext

fun isColorDark(color: Color): Boolean {
    return color.luminance() < 0.5
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GameCardHorizontal(
    game: Game,
    onClick: () -> Unit = {}
) {
    val libraryViewModel: LibraryViewModel =
        viewModel(factory = GameVaultAppViewModelProvider.Factory)
    val userGames by libraryViewModel.userGames.collectAsState()
    val isAlreadyInLibrary = userGames.any { it.gameId == game.id }
    val categoryInfo = when (game.game_type) {
        0 -> "Main game" to Color(0xFF6A1B9A)
        1 -> "DLC" to Color(0xFF6A1B9A)             // fioletowy
        2 -> "Expansion" to Color(0xFF1565C0)        // niebieski
        3 -> "Bundle" to Color(0xFF2E7D32)           // zielony
        4 -> "Standalone" to Color(0xFF00838F)       // ciemno-cyjan
        5 -> "Mod" to Color(0xFF455A64)              // szaro-niebieski
        6 -> "Episode" to Color(0xFF8D6E63)          // brązowy
        7 -> "Season" to Color(0xFFF57F17)           // żółto-pomarańczowy
        8 -> "Remake" to Color(0xFF4E342E)           // ciemny brąz
        9 -> "Remaster" to Color(0xFF5D4037)         // cieplejszy brąz
        10 -> "Expanded" to Color(0xFF00796B)        // zieleń morska
        11 -> "Port" to Color(0xFF546E7A)            // ciemnoszary
        12 -> "Fork" to Color(0xFF37474F)            // bardzo ciemny
        13 -> "Pack" to Color(0xFF6D4C41)            // cegła
        14 -> "Update" to Color(0xFF0288D1)          // błękit
        null -> null
        else -> null
    }
    val context = LocalContext.current

    val interactionSource = remember { MutableInteractionSource() }

    val imageUrl = game.cover?.imageId?.let {
        "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg"
    }
    val latestReleaseDateText = game.release_dates
        ?.mapNotNull { it.human }
        ?.mapNotNull {
            try {
                LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: Exception) {
                null
            }
        }
        ?.maxOrNull()
        ?.format(DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.ENGLISH))
        ?: game.first_release_date?.let {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.ENGLISH)
            Instant.ofEpochSecond(it)
                .atZone(ZoneOffset.UTC)
                .format(formatter)
        }

    val platforms = game.platforms?.take(3)?.joinToString(", ") { it.name }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        LibraryStatusDialog(
            gameTitle = game.name ?: "Unknown",
            onDismiss = { showDialog = false },
            onSave = { status: String ->
                libraryViewModel.addGame(
                    UserGameEntity(
                        gameId = game.id,
                        name = game.name ?: "Unknown",
                        coverUrl = game.cover?.imageId?.let {
                            "https://images.igdb.com/igdb/image/upload/t_cover_big/${it}.jpg"
                        },
                        status = status
                    )
                )
                showDialog = false
            }
        )
    }

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    showDialog = true
                },
                onTap = {
                    onClick()
                }
            )
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(modifier = Modifier.height(120.dp)) {
                imageUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = game.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight(),
                    )
                } ?: run {
                    Image(
                        painter = painterResource(id = R.drawable.pic_not_found),
                        contentDescription = game.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = game.name ?: "Unknown game",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )

                            latestReleaseDateText?.let {
                                Text(
                                    text = "Release date: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            platforms?.let {
                                Text(
                                    text = "Platforms: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }

                        val genreText = game.genres?.let { genres ->
                            val displayedGenres = genres.take(3).joinToString(", ") { it.name }
                            if (genres.size > 3) "$displayedGenres..." else displayedGenres
                        } ?: "Unknown genre"

                        Text(
                            text = genreText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!isAlreadyInLibrary) {
                                showDialog = true
                            } else {
                                Toast.makeText(context, "Game is already in your library", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(48.dp),
                        interactionSource = interactionSource,
                        enabled = true
                    ) {
                        Icon(
                            imageVector = if (isAlreadyInLibrary) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = if (isAlreadyInLibrary) "Already in library" else "Add to library",
                            tint = if (isAlreadyInLibrary) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    }

                    game.total_rating?.let {
                        Text(
                            text = "⭐ %.1f".format(it),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    categoryInfo?.let { (label, bgColor) ->
                        val textColor = if (isColorDark(bgColor)) Color.White else Color.Black

                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            ),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 4.dp, start = 12.dp)
                                .background(bgColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun GameCardVertical(
    game: Game,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val libraryViewModel: LibraryViewModel =
        viewModel(factory = GameVaultAppViewModelProvider.Factory)
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        LibraryStatusDialog(
            gameTitle = game.name ?: "Unknown",
            onDismiss = { showDialog = false },
            onSave = { status: String ->
                libraryViewModel.addGame(
                    UserGameEntity(
                        gameId = game.id,
                        name = game.name ?: "Unknown",
                        coverUrl = game.cover?.imageId?.let {
                            "https://images.igdb.com/igdb/image/upload/t_cover_big/${it}.jpg"
                        },
                        status = status
                    )
                )
                showDialog = false
            }
        )
    }

    Card(
        modifier = modifier
            .width(140.dp)
            .padding(8.dp)
            .clickable { onClick() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { showDialog = true }
                )
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageUrl = game.cover?.imageId?.let {
                "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg"
            }

            imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = game.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(110.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } ?: run {
                Image(
                    painter = painterResource(id = R.drawable.pic_not_found),
                    contentDescription = game.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(110.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // Game title
            Text(
                text = game.name ?: "",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .height(34.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Genres
                Text(
                    text = game.genres?.joinToString(", ") { it.name } ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Rating
                game.total_rating?.let {
                    Text(
                        text = "⭐ %.1f".format(it),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun GenreChips(
    genres: List<GenreWithGameCover>,
    onGenreClick: (GenreWithGameCover) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(genres) { genre ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(80.dp)
                    .clickable { onGenreClick(genre) }
            ) {
                genre.coverImageId?.let {
                    AsyncImage(
                        model = "https://images.igdb.com/igdb/image/upload/t_cover_big/${it}.jpg",
                        contentDescription = genre.genreName,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = genre.genreName,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

