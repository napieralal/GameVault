package com.example.gamevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.gamevault.ui.screens.search.SortDirection
import com.example.gamevault.ui.screens.search.SortField
import com.example.gamevault.ui.screens.search.SortType

@Composable
fun ExpandableText(
    title: String,
    text: String,
    maxLinesCollapsed: Int = 3,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    toggleColor: Color = MaterialTheme.colorScheme.primary
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = titleColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = text,
            maxLines = if (expanded) Int.MAX_VALUE else maxLinesCollapsed,
            overflow = TextOverflow.Ellipsis,
            color = textColor
        )
        Text(
            text = if (expanded) "Show less" else "Show more",
            color = toggleColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(top = 4.dp)
        )
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun FullscreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
fun SortMenu(
    currentSort: SortType,
    onSortSelected: (SortField) -> Unit,
    onToggleDirection: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val sortOptions = listOf(
        SortField.RELEVANCE,
        SortField.RATING,
        SortField.NAME,
        SortField.RELEASE_DATE,
        SortField.POPULARITY
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Dropdown button
        Button(onClick = { expanded = true }) {
            Text(text = currentSort.field.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() })
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sortOptions.forEach { sortField ->
                DropdownMenuItem(
                    text = {
                        Text(
                            sortField.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    },
                    onClick = {
                        onSortSelected(sortField)
                        expanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Direction toggle button
        val icon = if (currentSort.direction == SortDirection.ASC) {
            Icons.Default.ArrowUpward
        } else {
            Icons.Default.ArrowDownward
        }

        Icon(
            imageVector = icon,
            contentDescription = "Toggle sort direction",
            modifier = Modifier
                .clickable { onToggleDirection() }
                .size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}