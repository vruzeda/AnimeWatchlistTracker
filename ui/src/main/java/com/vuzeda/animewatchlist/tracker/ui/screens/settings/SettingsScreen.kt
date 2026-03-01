package com.vuzeda.animewatchlist.tracker.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vuzeda.animewatchlist.tracker.designsystem.component.ConfirmationDialog
import com.vuzeda.animewatchlist.tracker.ui.R

@Composable
fun SettingsScreenRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        onDeleteAllClick = viewModel::requestDeleteAllData,
        onConfirmDelete = viewModel::confirmDeleteAllData,
        onDismissDelete = viewModel::dismissDeleteConfirmation,
        onDataDeletedShown = viewModel::clearDataDeletedFlag
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onDeleteAllClick: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onDataDeletedShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val dataDeletedMessage = stringResource(R.string.settings_data_deleted)

    LaunchedEffect(uiState.isDataDeleted) {
        if (uiState.isDataDeleted) {
            snackbarHostState.showSnackbar(dataDeletedMessage)
            onDataDeletedShown()
        }
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )

            TextButton(
                onClick = onDeleteAllClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.settings_delete_all_data),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (uiState.isDeleteConfirmationVisible) {
            ConfirmationDialog(
                title = stringResource(R.string.settings_delete_dialog_title),
                message = stringResource(R.string.settings_delete_dialog_message),
                confirmText = stringResource(R.string.settings_delete_dialog_confirm),
                dismissText = stringResource(R.string.settings_delete_dialog_dismiss),
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete
            )
        }
    }
}
