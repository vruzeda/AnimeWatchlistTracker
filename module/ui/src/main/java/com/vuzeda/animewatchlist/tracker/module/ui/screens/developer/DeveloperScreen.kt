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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vuzeda.animewatchlist.tracker.module.ui.R

@Composable
fun DeveloperScreenRoute(
    onNavigateBack: () -> Unit,
    viewModel: DeveloperViewModel = hiltViewModel()
) {
    DeveloperScreen(
        onNavigateBack = onNavigateBack,
        onFireTestEpisodeNotification = viewModel::fireTestEpisodeNotification,
        onFireTestSeasonNotification = viewModel::fireTestSeasonNotification
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    onNavigateBack: () -> Unit,
    onFireTestEpisodeNotification: () -> Unit,
    onFireTestSeasonNotification: () -> Unit
) {
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

            TextButton(
                onClick = onFireTestEpisodeNotification,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.developer_test_episode_notification),
                    modifier = Modifier.weight(1f)
                )
            }

            TextButton(
                onClick = onFireTestSeasonNotification,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.developer_test_season_notification),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
