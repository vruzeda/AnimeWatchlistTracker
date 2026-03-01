package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vuzeda.animewatchlist.tracker.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.designsystem.component.EpisodeListItem
import com.vuzeda.animewatchlist.tracker.designsystem.component.EpisodeStepper
import com.vuzeda.animewatchlist.tracker.designsystem.component.RatingBar
import com.vuzeda.animewatchlist.tracker.designsystem.component.RelatedAnimeItem
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusChip
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusOption
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusSelectionSheet
import com.vuzeda.animewatchlist.tracker.domain.model.RelationType
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.R
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toColor
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toDisplayLabelRes

@Composable
fun DetailScreenRoute(
    onNavigateBack: () -> Unit,
    onNavigateToRelated: (Int) -> Unit,
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
        onToggleNotifications = viewModel::toggleNotifications,
        onAddClick = viewModel::showAddSheet,
        onAddStatusSelected = viewModel::addToWatchlist,
        onDismissAddSheet = viewModel::dismissAddSheet,
        onLoadMoreEpisodes = viewModel::loadMoreEpisodes,
        onRelatedAnimeClick = onNavigateToRelated
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
    onToggleNotifications: () -> Unit,
    onAddClick: () -> Unit,
    onAddStatusSelected: (WatchStatus) -> Unit,
    onDismissAddSheet: () -> Unit,
    onLoadMoreEpisodes: () -> Unit,
    onRelatedAnimeClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.detail_title)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back)
                    )
                }
            },
            actions = {
                if (uiState is DetailUiState.Success) {
                    if (uiState.isInWatchlist) {
                        if (uiState.anime.malId != null) {
                            IconButton(onClick = onToggleNotifications) {
                                Icon(
                                    imageVector = if (uiState.isNotificationsEnabled) {
                                        Icons.Default.Notifications
                                    } else {
                                        Icons.Default.NotificationsNone
                                    },
                                    contentDescription = stringResource(
                                        if (uiState.isNotificationsEnabled) R.string.cd_disable_notifications
                                        else R.string.cd_enable_notifications
                                    )
                                )
                            }
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_delete)
                            )
                        }
                    } else {
                        IconButton(onClick = onAddClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.cd_add_to_watchlist),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
                    title = stringResource(R.string.detail_not_found)
                )
            }
            is DetailUiState.Success -> {
                DetailContent(
                    state = uiState,
                    onStatusChipClick = onStatusChipClick,
                    onEpisodeChanged = onEpisodeChanged,
                    onRatingChanged = onRatingChanged,
                    onLoadMoreEpisodes = onLoadMoreEpisodes,
                    onRelatedAnimeClick = onRelatedAnimeClick
                )

                if (uiState.isStatusSheetVisible) {
                    val statusOptions = WatchStatus.entries.map {
                        StatusOption(stringResource(it.toDisplayLabelRes()), it.toColor())
                    }
                    StatusSelectionSheet(
                        title = stringResource(R.string.detail_change_status_title),
                        subtitle = uiState.anime.title,
                        options = statusOptions,
                        onOptionSelected = { index ->
                            onStatusSelected(WatchStatus.entries[index])
                            onDismissStatusSheet()
                        },
                        onDismiss = onDismissStatusSheet
                    )
                }

                if (uiState.isAddSheetVisible) {
                    val statusOptions = WatchStatus.entries.map {
                        StatusOption(stringResource(it.toDisplayLabelRes()), it.toColor())
                    }
                    StatusSelectionSheet(
                        title = stringResource(R.string.detail_add_sheet_title),
                        subtitle = uiState.anime.title,
                        options = statusOptions,
                        onOptionSelected = { index ->
                            onAddStatusSelected(WatchStatus.entries[index])
                        },
                        onDismiss = onDismissAddSheet
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
    onRatingChanged: (Int) -> Unit,
    onLoadMoreEpisodes: () -> Unit,
    onRelatedAnimeClick: (Int) -> Unit
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
                        label = stringResource(anime.status.toDisplayLabelRes()),
                        color = anime.status.toColor(),
                        modifier = Modifier.clickable(onClick = onStatusChipClick)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (anime.score != null) {
                    Text(
                        text = stringResource(R.string.detail_mal_score, anime.score.toString()),
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
                text = stringResource(R.string.detail_section_progress),
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
                text = stringResource(R.string.detail_section_your_rating),
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
                text = stringResource(R.string.detail_episode_count, anime.episodeCount!!),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        val synopsis = anime.synopsis
        if (synopsis != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.detail_section_synopsis),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = synopsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.relatedAnime.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.detail_section_related),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.relatedAnime.forEach { related ->
                    val relationLabel = when (related.relationType) {
                        RelationType.PREQUEL -> stringResource(R.string.detail_relation_prequel)
                        RelationType.SEQUEL -> stringResource(R.string.detail_relation_sequel)
                    }
                    val relationColor = when (related.relationType) {
                        RelationType.PREQUEL -> MaterialTheme.colorScheme.secondary
                        RelationType.SEQUEL -> MaterialTheme.colorScheme.primary
                    }
                    RelatedAnimeItem(
                        title = related.title,
                        relationLabel = relationLabel,
                        relationColor = relationColor,
                        onClick = { onRelatedAnimeClick(related.malId) }
                    )
                }
            }
        } else if (state.isLoadingRelated) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        if (state.episodes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.detail_section_episodes),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            state.episodes.forEachIndexed { index, episode ->
                EpisodeListItem(
                    episodeNumber = episode.number,
                    title = episode.title,
                    airedDate = episode.aired,
                    isFiller = episode.isFiller,
                    isRecap = episode.isRecap,
                    showDivider = index < state.episodes.lastIndex || state.hasMoreEpisodes
                )
            }
            if (state.hasMoreEpisodes) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onLoadMoreEpisodes,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoadingEpisodes
                ) {
                    if (state.isLoadingEpisodes) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text(stringResource(R.string.detail_load_more_episodes))
                    }
                }
            }
        } else if (state.isLoadingEpisodes) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

