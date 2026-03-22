package com.vuzeda.animewatchlist.tracker.module.ui.screens.developer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vuzeda.animewatchlist.tracker.module.ui.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant

@Composable
fun DeveloperScreenRoute(
    onNavigateBack: () -> Unit,
    viewModel: DeveloperViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DeveloperScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTriggerAnimeUpdate = viewModel::triggerAnimeUpdate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    uiState: DeveloperUiState,
    onNavigateBack: () -> Unit,
    onTriggerAnimeUpdate: () -> Unit
) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault())
    }

    Scaffold(contentWindowInsets = WindowInsets(0)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopAppBar(
                title = { Text(stringResource(R.string.developer_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )

            Text(
                text = stringResource(R.string.developer_last_update_run),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Text(
                text = uiState.lastAnimeUpdateRun?.formatWith(formatter)
                    ?: stringResource(R.string.developer_last_update_run_never),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            TextButton(
                onClick = onTriggerAnimeUpdate,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.developer_trigger_update),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun Instant.formatWith(formatter: DateTimeFormatter): String =
    formatter.format(java.time.Instant.ofEpochMilli(toEpochMilliseconds()))
