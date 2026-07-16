package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.NotificationButton

@Composable
fun NotificationButtonWithPermissions(
    enabled: Boolean,
    onClick: () -> Unit,
    onPermissionDenied: () -> Unit,
) {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }
    val waitingForSettingsResult = remember { mutableStateOf(false) }

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnPermissionDenied by rememberUpdatedState(onPermissionDenied)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && waitingForSettingsResult.value) {
                waitingForSettingsResult.value = false
                val granted = Build.VERSION_CODES.TIRAMISU > Build.VERSION.SDK_INT ||
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    val handleClick: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    NotificationButton(enabled = enabled, onClick = handleClick)
}
