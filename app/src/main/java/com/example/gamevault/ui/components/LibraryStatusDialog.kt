package com.example.gamevault.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LibraryStatusDialog(
    gameTitle: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val statuses = listOf(
        "WANT_TO_PLAY", "PLAYING", "COMPLETED", "UNSPECIFIED"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Save \"$gameTitle\" to Library")
        },
        text = {
            Column {
                statuses.forEach { status ->
                    Text(
                        text = status,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSave(status) }
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Text(
                text = "Cancel",
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(8.dp)
            )
        }
    )
}