package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.ConfirmationDialog
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.EpisodeListItem
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.NotificationButton
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.StatusChip
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.StatusOption
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.StatusSelectionSheet
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.resolveDisplayTitle
import com.vuzeda.animewatchlist.tracker.module.ui.R
import com.vuzeda.animewatchlist.tracker.module.ui.screens.home.toColor
import com.vuzeda.animewatchlist.tracker.module.ui.screens.home.toDisplayLabelRes

@Composable
fun SeasonDetailScreenRoute(
    onNavigateBack: () -> Unit,
    onNavigateToAnimeDetail: (Int) -> Unit,
    viewModel: SeasonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        onStatusChipClick = viewModel::showStatusSheet,
        onStatusSelected = viewModel::updateStatus,
        onDismissStatusSheet = viewModel::dismissStatusSheet,
        onEpisodeWatched = viewModel::setEpisodeWatched,
        onMarkAllEpisodesWatched = viewModel::markAllEpisodesWatched,
        onLoadMoreEpisodes = viewModel::loadMoreEpisodes,
        onDeleteClick = viewModel::showDeleteConfirmation,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDeleteConfirmation = viewModel::dismissDeleteConfirmation,
        onAddToWatchlistClick = viewModel::showAddSheet,
        onAddStatusSelected = viewModel::addToWatchlist,
        onDismissAddSheet = viewModel::dismissAddSheet,
        onToggleEpisodeNotifications = viewModel::toggleEpisodeNotifications,
        onViewFullSeriesClick = viewModel::navigateToAnimeDetail,
        onSnackbarDismissed = viewModel::clearSnackbar,
        onNotificationPermissionDenied = viewModel::notifyPermissionDenied,
        onRefresh = viewModel::refresh
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonDetailScreen(
    uiState: SeasonDetailUiState,
    onNavigateBack: () -> Unit,
    onStatusChipClick: () -> Unit,
    onStatusSelected: (WatchStatus) -> Unit,
    onDismissStatusSheet: () -> Unit,
    onEpisodeWatched: (Int, Boolean) -> Unit,
    onMarkAllEpisodesWatched: () -> Unit,
    onLoadMoreEpisodes: () -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    onAddToWatchlistClick: () -> Unit,
    onAddStatusSelected: (WatchStatus) -> Unit,
    onDismissAddSheet: () -> Unit,
    onToggleEpisodeNotifications: () -> Unit,
    onViewFullSeriesClick: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    onNotificationPermissionDenied: () -> Unit,
    onRefresh: () -> Unit = {}
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
                            onPermissionDenied = onNotificationPermissionDenied
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
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        SeasonDetailContent(
                            state = uiState,
                            onStatusChipClick = onStatusChipClick,
                            onEpisodeWatched = onEpisodeWatched,
                            onMarkAllEpisodesWatched = onMarkAllEpisodesWatched,
                            onLoadMoreEpisodes = onLoadMoreEpisodes,
                            onAddToWatchlistClick = onAddToWatchlistClick,
                            onViewFullSeriesClick = onViewFullSeriesClick
                        )
                    }

                    if (uiState.isStatusSheetVisible) {
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
                            title = stringResource(R.string.anime_detail_change_status_title),
                            subtitle = displayTitle,
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
                            title = stringResource(
                                if (uiState.isLastSeason) R.string.delete_anime_dialog_title
                                else R.string.delete_season_dialog_title
                            ),
                            message = stringResource(
                                if (uiState.isLastSeason) R.string.delete_anime_dialog_message
                                else R.string.delete_season_dialog_message
                            ),
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
    onStatusChipClick: () -> Unit,
    onEpisodeWatched: (Int, Boolean) -> Unit,
    onMarkAllEpisodesWatched: () -> Unit,
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
            broadcastLocalTime = state.broadcastLocalTime,
            onStatusChipClick = onStatusChipClick,
            onAddToWatchlistClick = onAddToWatchlistClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onViewFullSeriesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.season_detail_view_full_series))
        }

        if (season.streamingLinks.isNotEmpty()) {
            val context = LocalContext.current
            var streamingExpanded by remember { mutableStateOf(false) }
            val collapsedLimit = 3
            val hasMore = season.streamingLinks.size > collapsedLimit
            val visibleLinks = if (streamingExpanded || !hasMore) season.streamingLinks
                               else season.streamingLinks.take(collapsedLimit)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.season_detail_section_streaming),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            visibleLinks.forEach { link ->
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(link.name)
                }
            }
            if (hasMore) {
                OutlinedButton(
                    onClick = { streamingExpanded = !streamingExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (streamingExpanded) stringResource(R.string.season_detail_streaming_show_less)
                        else stringResource(R.string.season_detail_streaming_show_more, season.streamingLinks.size - collapsedLimit)
                    )
                }
            }
        }

        if (state.episodes.isNotEmpty() || state.isLoadingEpisodes) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.season_detail_section_episodes),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (state.isInWatchlist && state.episodes.isNotEmpty()) {
                    var isMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_episodes_menu)
                            )
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.season_detail_mark_all_episodes_watched)) },
                                onClick = {
                                    onMarkAllEpisodesWatched()
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            state.episodes.forEachIndexed { index, episode ->
                EpisodeListItem(
                    episodeNumber = episode.number,
                    title = episode.title,
                    airedDate = episode.aired,
                    isFiller = episode.isFiller,
                    isRecap = episode.isRecap,
                    showDivider = index < state.episodes.size - 1 || state.hasMoreEpisodes,
                    isWatched = episode.number in state.watchedEpisodes,
                    onWatchedToggle = if (state.isInWatchlist) {
                        { onEpisodeWatched(episode.number, episode.number !in state.watchedEpisodes) }
                    } else null
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

        if (state.isInWatchlist && state.isNotificationDebugInfoEnabled) {
            Text(
                text = stringResource(
                    R.string.developer_last_checked_aired_episodes,
                    state.season.lastCheckedAiredEpisodeCount?.toString()
                        ?: stringResource(R.string.developer_value_none)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Text(
                text = stringResource(
                    R.string.developer_last_episode_check,
                    state.season.lastEpisodeCheckDate?.toString()
                        ?: stringResource(R.string.developer_value_never)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun SeasonHeaderSection(
    season: Season,
    titleLanguage: TitleLanguage,
    isInWatchlist: Boolean,
    broadcastLocalTime: LocalBroadcastTime?,
    onStatusChipClick: () -> Unit,
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

            val broadcastInfo = season.broadcastInfo
            if (broadcastInfo != null) {
                Text(
                    text = stringResource(R.string.season_detail_broadcast_info, broadcastInfo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (broadcastLocalTime != null) {
                Text(
                    text = stringResource(
                        R.string.season_detail_broadcast_local_time,
                        stringResource(R.string.season_detail_local_broadcast_format, broadcastLocalTime.day, broadcastLocalTime.time, broadcastLocalTime.zone)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            if (isInWatchlist) {
                StatusChip(
                    label = stringResource(season.status.toDisplayLabelRes()),
                    color = season.status.toColor(),
                    modifier = Modifier.clickable(onClick = onStatusChipClick)
                )
            } else {
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
    is SeasonDetailSnackbarEvent.NotificationPermissionDenied ->
        stringResource(R.string.notification_permission_denied)
}
