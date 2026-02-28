package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vuzeda.animewatchlist.tracker.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.designsystem.component.EpisodeStepper
import com.vuzeda.animewatchlist.tracker.designsystem.component.RatingBar
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusChip
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusOption
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusSelectionSheet
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toColor
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toDisplayLabel

@Composable
fun DetailScreenRoute(
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onStatusChipClick = viewModel::showStatusSheet,
        onStatusSelected = viewModel::updateStatus,
        onDismissStatusSheet = viewModel::dismissStatusSheet,
        onEpisodeChanged = viewModel::updateCurrentEpisode,
        onRatingChanged = viewModel::updateUserRating,
        onDelete = { viewModel.deleteAnime(onNavigateBack) },
        onToggleNotifications = viewModel::toggleNotifications
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onNavigateBack: () -> Unit,
    onStatusChipClick: () -> Unit,
    onStatusSelected: (WatchStatus) -> Unit,
    onDismissStatusSheet: () -> Unit,
    onEpisodeChanged: (Int) -> Unit,
    onRatingChanged: (Int) -> Unit,
    onDelete: () -> Unit,
    onToggleNotifications: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Anime Detail") },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                if (uiState is DetailUiState.Success && uiState.isInWatchlist) {
                    if (uiState.anime.malId != null) {
                        IconButton(onClick = onToggleNotifications) {
                            Icon(
                                imageVector = if (uiState.isNotificationsEnabled) {
                                    Icons.Default.Notifications
                                } else {
                                    Icons.Default.NotificationsNone
                                },
                                contentDescription = if (uiState.isNotificationsEnabled) {
                                    "Disable notifications"
                                } else {
                                    "Enable notifications"
                                }
                            )
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        )

        when (uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DetailUiState.NotFound -> {
                EmptyStateMessage(
                    modifier = Modifier.fillMaxSize(),
                    title = "Anime not found"
                )
            }
            is DetailUiState.Success -> {
                DetailContent(
                    state = uiState,
                    onStatusChipClick = onStatusChipClick,
                    onEpisodeChanged = onEpisodeChanged,
                    onRatingChanged = onRatingChanged
                )

                if (uiState.isStatusSheetVisible) {
                    val statusOptions = remember {
                        WatchStatus.entries.map { StatusOption(it.toDisplayLabel(), it.toColor()) }
                    }
                    StatusSelectionSheet(
                        title = "Change status",
                        subtitle = uiState.anime.title,
                        options = statusOptions,
                        onOptionSelected = { index ->
                            onStatusSelected(WatchStatus.entries[index])
                            onDismissStatusSheet()
                        },
                        onDismiss = onDismissStatusSheet
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState.Success,
    onStatusChipClick: () -> Unit,
    onEpisodeChanged: (Int) -> Unit,
    onRatingChanged: (Int) -> Unit
) {
    val anime = state.anime
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = anime.imageUrl,
                contentDescription = anime.title,
                modifier = Modifier
                    .width(120.dp)
                    .height(170.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color(0xFFE0E0E0)),
                error = ColorPainter(Color(0xFFE0E0E0)),
                fallback = ColorPainter(Color(0xFFE0E0E0))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (state.isInWatchlist) {
                    StatusChip(
                        label = anime.status.toDisplayLabel(),
                        color = anime.status.toColor(),
                        modifier = Modifier.clickable(onClick = onStatusChipClick)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (anime.score != null) {
                    Text(
                        text = "MAL Score: ★ ${anime.score}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (anime.genres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = anime.genres.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (state.isInWatchlist) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Progress",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            EpisodeStepper(
                currentEpisode = anime.currentEpisode,
                totalEpisodes = anime.episodeCount,
                onEpisodeChanged = onEpisodeChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Rating",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            RatingBar(
                rating = anime.userRating ?: 0,
                isInteractive = true,
                onRatingChanged = onRatingChanged
            )
        } else if (anime.episodeCount != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "${anime.episodeCount} episodes",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        val synopsis = anime.synopsis
        if (synopsis != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Synopsis",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = synopsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

