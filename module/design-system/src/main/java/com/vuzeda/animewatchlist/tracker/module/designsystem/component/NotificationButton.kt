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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme

@Composable
fun NotificationButton(
    enabled: Boolean,
    onClick: () -> Unit,
    onPermissionDenied: () -> Unit,
) {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }

    // Explicit MutableState reference so the DisposableEffect observer always reads the
    // current value without relying on Kotlin delegate capture semantics in a lambda.
    val waitingForSettingsResult = remember { mutableStateOf(false) }

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnPermissionDenied by rememberUpdatedState(onPermissionDenied)

    // When the user returns from the Settings app, check if permission was granted and
    // proceed with enabling notifications if so.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && waitingForSettingsResult.value) {
                waitingForSettingsResult.value = false
                val granted = Build.VERSION.SDK_INT < 33 ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                if (granted) currentOnClick() else currentOnPermissionDenied()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onClick()
        } else if (Build.VERSION.SDK_INT >= 33) {
            val activity = context as? ComponentActivity
            val permanentlyDenied = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
            if (permanentlyDenied) {
                showRationaleDialog = true
            } else {
                onPermissionDenied()
            }
        } else {
            onPermissionDenied()
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

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = {
                showRationaleDialog = false
                onPermissionDenied()
            },
            title = { Text(stringResource(R.string.notification_permission_rationale_title)) },
            text = { Text(stringResource(R.string.notification_permission_rationale_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    waitingForSettingsResult.value = true
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                    )
                }) {
                    Text(stringResource(R.string.notification_permission_go_to_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    onPermissionDenied()
                }) {
                    Text(stringResource(R.string.notification_permission_cancel))
                }
            }
        )
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
            onClick = {},
            onPermissionDenied = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationButtonEnabledPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        NotificationButton(
            enabled = true,
            onClick = {},
            onPermissionDenied = {}
        )
    }
}
