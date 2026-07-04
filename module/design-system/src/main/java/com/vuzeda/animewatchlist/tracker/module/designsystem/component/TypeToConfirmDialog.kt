package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing

@Composable
fun TypeToConfirmDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    confirmationPhrase: String,
    confirmationHint: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmColor: Color = MaterialTheme.colorScheme.error
) {
    var inputText by remember { mutableStateOf("") }
    val confirmed = inputText == confirmationPhrase

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                Text(text = message)
                Spacer(modifier = Modifier.height(ElementSpacing))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(text = confirmationHint) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmed
            ) {
                Text(
                    text = confirmText,
                    color = if (confirmed) confirmColor
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
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
private fun TypeToConfirmDialogEmptyPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        TypeToConfirmDialog(
            title = "Delete all data?",
            message = "This will permanently remove all anime and seasons from your watchlist. This action cannot be undone.",
            confirmationPhrase = "DELETE",
            confirmationHint = "Type DELETE to confirm",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

