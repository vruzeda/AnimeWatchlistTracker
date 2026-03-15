package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme

@Composable
fun NotificationButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onClick()
        } else if (Build.VERSION.SDK_INT >= 33) {
            val activity = context as? ComponentActivity
            val permanentlyDenied = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
            if (permanentlyDenied) {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                )
            }
        }
    }

    val checkPermissionOnClick: () -> Unit = {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                onClick()
            } else {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            onClick()
        }
    }

    IconButton(onClick = checkPermissionOnClick) {
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
