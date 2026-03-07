package com.vuzeda.animewatchlist.tracker.ui.screens.seasondetail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.vuzeda.animewatchlist.tracker.designsystem.component.ConfirmationDialog
import com.vuzeda.animewatchlist.tracker.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.designsystem.component.EpisodeListItem
import com.vuzeda.animewatchlist.tracker.designsystem.component.EpisodeStepper
import com.vuzeda.animewatchlist.tracker.designsystem.component.NotificationButton
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusOption
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusSelectionSheet
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.model.resolveDisplayTitle
import com.vuzeda.animewatchlist.tracker.ui.R
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toColor
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toDisplayLabelRes

@Composable
fun SeasonDetailScreenRoute(
    onNavigateBack: () -> Unit,
    onNavigateToAnimeDetail: (Int) -> Unit,
    viewModel: SeasonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState is SeasonDetailUiState.Success && (uiState as SeasonDetailUiState.Success).isDeleted) {
        LaunchedEffect(Unit) { onNavigateBack() }
    }

    LaunchedEffect((uiState as? SeasonDetailUiState.Success)?.pendingNavigationMalId) {
        val malId = (uiState as? SeasonDetailUiState.Success)?.pendingNavigationMalId
        if (malId != null) {
            viewModel.onNavigated()
            onNavigateToAnimeDetail(malId)
        }
    }

    SeasonDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onEpisodeProgressChanged = viewModel::updateEpisodeProgress,
        onLoadMoreEpisodes = viewModel::loadMoreEpisodes,
        onDeleteClick = viewModel::showDeleteConfirmation,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDeleteConfirmation = viewModel::dismissDeleteConfirmation,
        onAddToWatchlistClick = viewModel::showAddSheet,
        onAddStatusSelected = viewModel::addToWatchlist,
        onDismissAddSheet = viewModel::dismissAddSheet,
        onToggleEpisodeNotifications = viewModel::toggleEpisodeNotifications,
        onViewFullSeriesClick = viewModel::navigateToAnimeDetail,
        onSnackbarDismissed = viewModel::clearSnackbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonDetailScreen(
    uiState: SeasonDetailUiState,
    onNavigateBack: () -> Unit,
    onEpisodeProgressChanged: (Int) -> Unit,
    onLoadMoreEpisodes: () -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    onAddToWatchlistClick: () -> Unit,
    onAddStatusSelected: (WatchStatus) -> Unit,
    onDismissAddSheet: () -> Unit,
    onToggleEpisodeNotifications: () -> Unit,
    onViewFullSeriesClick: () -> Unit,
    onSnackbarDismissed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState is SeasonDetailUiState.Success && uiState.snackbarEvent != null) {
        val message = resolveSnackbarMessage(uiState.snackbarEvent)
        LaunchedEffect(uiState.snackbarEvent) {
            snackbarHostState.showSnackbar(message)
            onSnackbarDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.season_detail_title)) },
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
                    if (uiState is SeasonDetailUiState.Success && uiState.isInWatchlist) {
                        NotificationButton(
                            enabled = uiState.isEpisodeNotificationsEnabled,
                            onClick = onToggleEpisodeNotifications,
                        )
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_delete)
                            )
                        }
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            when (uiState) {
                is SeasonDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is SeasonDetailUiState.NotFound -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.season_detail_not_found)
                    )
                }
                is SeasonDetailUiState.Success -> {
                    SeasonDetailContent(
                        state = uiState,
                        onEpisodeProgressChanged = onEpisodeProgressChanged,
                        onLoadMoreEpisodes = onLoadMoreEpisodes,
                        onAddToWatchlistClick = onAddToWatchlistClick,
                        onViewFullSeriesClick = onViewFullSeriesClick
                    )

                    if (uiState.isAddSheetVisible) {
                        val statusOptions = WatchStatus.entries.map {
                            StatusOption(stringResource(it.toDisplayLabelRes()), it.toColor())
                        }
                        val displayTitle = resolveDisplayTitle(
                            title = uiState.season.title,
                            titleEnglish = uiState.season.titleEnglish,
                            titleJapanese = uiState.season.titleJapanese,
                            language = uiState.titleLanguage
                        )
                        StatusSelectionSheet(
                            title = stringResource(R.string.season_detail_add_sheet_title),
                            subtitle = displayTitle,
                            options = statusOptions,
                            onOptionSelected = { index ->
                                onAddStatusSelected(WatchStatus.entries[index])
                            },
                            onDismiss = onDismissAddSheet
                        )
                    }

                    if (uiState.isDeleteConfirmationVisible) {
                        ConfirmationDialog(
                            title = stringResource(R.string.delete_anime_dialog_title),
                            message = stringResource(R.string.delete_anime_dialog_message),
                            confirmText = stringResource(R.string.delete_anime_dialog_confirm),
                            dismissText = stringResource(R.string.delete_anime_dialog_dismiss),
                            onConfirm = onConfirmDelete,
                            onDismiss = onDismissDeleteConfirmation
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonDetailContent(
    state: SeasonDetailUiState.Success,
    onEpisodeProgressChanged: (Int) -> Unit,
    onLoadMoreEpisodes: () -> Unit,
    onAddToWatchlistClick: () -> Unit,
    onViewFullSeriesClick: () -> Unit
) {
    val season = state.season
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SeasonHeaderSection(
            season = season,
            titleLanguage = state.titleLanguage,
            isInWatchlist = state.isInWatchlist,
            onAddToWatchlistClick = onAddToWatchlistClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onViewFullSeriesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.season_detail_view_full_series))
        }

        if (state.isInWatchlist) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.season_detail_section_progress),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            EpisodeStepper(
                currentEpisode = season.currentEpisode,
                totalEpisodes = season.episodeCount,
                onEpisodeChanged = onEpisodeProgressChanged
            )
        }

        if (state.episodes.isNotEmpty() || state.isLoadingEpisodes) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.season_detail_section_episodes),
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
                    showDivider = index < state.episodes.size - 1 || state.hasMoreEpisodes
                )
            }

            if (state.isLoadingEpisodes) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.hasMoreEpisodes) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onLoadMoreEpisodes,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.season_detail_load_more_episodes))
                }
            }
        }
    }
}

@Composable
private fun SeasonHeaderSection(
    season: Season,
    titleLanguage: TitleLanguage,
    isInWatchlist: Boolean,
    onAddToWatchlistClick: () -> Unit
) {
    val displayTitle = resolveDisplayTitle(
        title = season.title,
        titleEnglish = season.titleEnglish,
        titleJapanese = season.titleJapanese,
        language = titleLanguage
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = season.imageUrl,
            contentDescription = displayTitle,
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

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = season.type,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (season.score != null) {
                Text(
                    text = stringResource(R.string.season_detail_mal_score, season.score.toString()),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val totalEpisodes = season.episodeCount
            if (totalEpisodes != null) {
                Text(
                    text = stringResource(R.string.season_detail_episode_count, totalEpisodes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val airingStatus = season.airingStatus
            if (airingStatus != null) {
                Text(
                    text = stringResource(R.string.season_detail_airing_status, airingStatus),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isInWatchlist) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = onAddToWatchlistClick) {
                    Text(stringResource(R.string.season_detail_add_to_watchlist))
                }
            }
        }
    }
}

@Composable
private fun resolveSnackbarMessage(event: SeasonDetailSnackbarEvent): String = when (event) {
    is SeasonDetailSnackbarEvent.AddedToWatchlist -> stringResource(
        R.string.season_detail_added_to_watchlist,
        event.title
    )
    is SeasonDetailSnackbarEvent.EpisodeNotificationsToggled -> stringResource(
        if (event.enabled) R.string.season_detail_episode_notifications_enabled
        else R.string.season_detail_episode_notifications_disabled
    )
}
