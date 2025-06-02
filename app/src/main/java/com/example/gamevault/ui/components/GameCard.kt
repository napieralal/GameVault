package com.example.gamevault.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gamevault.model.Game

@Composable
fun GameCardHorizontal(
    game: Game,
    onClick: () -> Unit = {}
) {
    val imageUrl = game.cover?.imageId?.let {
        "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg"
    }

    Box {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
            Row(modifier = Modifier.height(120.dp)) {
                imageUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = game.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = game.name ?: "Unknown game",
                        style = MaterialTheme.typography.titleMedium
                    )

                    val genreText = game.genres?.let { genres ->
                        val displayedGenres = genres.take(3).joinToString(", ") { it.name }
                        if (genres.size > 3) "$displayedGenres..." else displayedGenres
                    } ?: "Unknown genre"
                    Text(
                        text = genreText,
                        style = MaterialTheme.typography.titleMedium
                    )
                    /*Text(
                        text = "Ocena: ${game.totalRating?.let { String.format("%.1f", it) } ?: "Brak"}",
                        style = MaterialTheme.typography.bodySmall
                    )*/
                }
            }
        }
        game.total_rating?.let {
            Text(
                text = "⭐ %.1f".format(it),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun GameCardVertical(game: Game, onclick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onclick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        game.cover?.imageId?.let {
            val url = "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg"
            AsyncImage(model = url, contentDescription = null)
        }

        Text(
            game.name ?: "",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = game.genres?.joinToString(", ") { it.name } ?: "",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            game.total_rating?.let {
                Text(
                    text = "⭐ %.1f".format(it),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

