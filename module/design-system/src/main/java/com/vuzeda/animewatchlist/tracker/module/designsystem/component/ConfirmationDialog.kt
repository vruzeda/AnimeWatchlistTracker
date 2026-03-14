package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme

@Composable
fun ConfirmationDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ConfirmationDialogPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        ConfirmationDialog(
            title = "Delete all data?",
            message = "This will permanently remove all anime and seasons from your watchlist. This action cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
