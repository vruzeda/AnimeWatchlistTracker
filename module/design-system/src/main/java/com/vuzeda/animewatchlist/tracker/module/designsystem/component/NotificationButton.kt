package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme

@Composable
fun NotificationButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (enabled) {
                Icons.Default.Notifications
            } else {
                Icons.Default.NotificationsNone
            },
            contentDescription = stringResource(
                if (enabled) {
                    R.string.cd_disable_notifications
                } else {
                    R.string.cd_enable_notifications
                }
            )
        )
    }
}

@Preview
@Composable
private fun NotificationButtonPreview() {
    AnimeWatchlistTrackerTheme {
        NotificationButton(enabled = true, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationButtonDisabledPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        NotificationButton(
            enabled = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationButtonEnabledPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        NotificationButton(
            enabled = true,
            onClick = {}
        )
    }
}
