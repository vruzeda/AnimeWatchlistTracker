package com.vuzeda.animewatchlist.tracker.module.ui.screens.animedetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.AnimeCard
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.ConfirmationDialog
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.NotificationButton
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.OptionSelectionSheet
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.RatingBar
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.StatusChip
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.StatusOption
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.StatusSelectionSheet
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.resolveDisplayTitle
import com.vuzeda.animewatchlist.tracker.module.ui.R
import com.vuzeda.animewatchlist.tracker.module.ui.screens.home.toColor
import com.vuzeda.animewatchlist.tracker.module.ui.screens.home.toDisplayLabelRes

@Composable
fun AnimeDetailScreenRoute(
    onNavigateBack: () -> Unit,
    onSeasonClick: (seasonId: Long, malId: Int) -> Unit,
    viewModel: AnimeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimeDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSeasonClick = onSeasonClick,
        onStatusChipClick = viewModel::showStatusSheet,
        onStatusSelected = viewModel::updateStatus,
        onDismissStatusSheet = viewModel::dismissStatusSheet,
        onRatingChanged = viewModel::updateUserRating,
        onDeleteClick = viewModel::showDeleteConfirmation,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDeleteConfirmation = viewModel::dismissDeleteConfirmation,
        onNotificationIconClick = viewModel::onNotificationIconClick,
        onSelectNotificationType = viewModel::selectNotificationType,
        onDismissNotificationTypeSheet = viewModel::dismissNotificationTypeSheet,
        onAddToWatchlistClick = viewModel::showAddSheet,
        onAddStatusSelected = viewModel::showAddScopeSheet,
        onDismissAddSheet = viewModel::dismissAddSheet,
        onConfirmAddScope = viewModel::confirmAddScope,
        onDismissAddScopeSheet = viewModel::dismissAddScopeSheet,
        onSeasonAddClick = viewModel::showAddSeasonSheet,
        onAddSeasonStatusSelected = viewModel::confirmAddSeason,
        onDismissAddSeasonSheet = viewModel::dismissAddSeasonSheet,
        onSnackbarDismissed = viewModel::clearSnackbar,
        onNotificationPermissionDenied = viewModel::notifyPermissionDenied
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    uiState: AnimeDetailUiState,
    onNavigateBack: () -> Unit,
    onSeasonClick: (seasonId: Long, malId: Int) -> Unit,
    onStatusChipClick: () -> Unit,
    onStatusSelected: (WatchStatus) -> Unit,
    onDismissStatusSheet: () -> Unit,
    onRatingChanged: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    onNotificationIconClick: () -> Unit,
    onSelectNotificationType: (NotificationType) -> Unit,
    onDismissNotificationTypeSheet: () -> Unit,
    onAddToWatchlistClick: () -> Unit,
    onAddStatusSelected: (WatchStatus) -> Unit,
    onDismissAddSheet: () -> Unit,
    onConfirmAddScope: (Boolean) -> Unit,
    onDismissAddScopeSheet: () -> Unit,
    onSeasonAddClick: (Season) -> Unit,
    onAddSeasonStatusSelected: (WatchStatus) -> Unit,
    onDismissAddSeasonSheet: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    onNotificationPermissionDenied: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState is AnimeDetailUiState.Success && uiState.snackbarEvent != null) {
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
                title = { Text(stringResource(R.string.anime_detail_title)) },
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
                    if (uiState is AnimeDetailUiState.Success && uiState.isInWatchlist) {
                        NotificationButton(
                            enabled = uiState.isNotificationsEnabled,
                            onClick = onNotificationIconClick,
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
                is AnimeDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is AnimeDetailUiState.NotFound -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.anime_detail_not_found)
                    )
                }
                is AnimeDetailUiState.Success -> {
                    AnimeDetailContent(
                        state = uiState,
                        onStatusChipClick = onStatusChipClick,
                        onRatingChanged = onRatingChanged,
                        onAddToWatchlistClick = onAddToWatchlistClick,
                        onSeasonClick = onSeasonClick,
                        onSeasonAddClick = onSeasonAddClick
                    )

                    val animeDisplayTitle = resolveDisplayTitle(
                        title = uiState.anime.title,
                        titleEnglish = uiState.anime.titleEnglish,
                        titleJapanese = uiState.anime.titleJapanese,
                        language = uiState.titleLanguage
                    )

                    if (uiState.isStatusSheetVisible) {
                        val statusOptions = WatchStatus.entries.map {
                            StatusOption(stringResource(it.toDisplayLabelRes()), it.toColor())
                        }
                        StatusSelectionSheet(
                            title = stringResource(R.string.anime_detail_change_status_title),
                            subtitle = animeDisplayTitle,
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
                            title = stringResource(R.string.anime_detail_add_sheet_title),
                            subtitle = animeDisplayTitle,
                            options = statusOptions,
                            onOptionSelected = { index ->
                                onAddStatusSelected(WatchStatus.entries[index])
                            },
                            onDismiss = onDismissAddSheet
                        )
                    }

                    if (uiState.isAddScopeSheetVisible) {
                        val addScopeOptions = listOf(
                            stringResource(R.string.anime_detail_add_scope_all),
                            stringResource(R.string.anime_detail_add_scope_first_only)
                        )
                        OptionSelectionSheet(
                            title = stringResource(R.string.anime_detail_add_scope_title),
                            subtitle = animeDisplayTitle,
                            options = addScopeOptions,
                            onOptionSelected = { index ->
                                onConfirmAddScope(index == 0)
                            },
                            onDismiss = onDismissAddScopeSheet
                        )
                    }

                    if (uiState.isAddSeasonSheetVisible) {
                        val statusOptions = WatchStatus.entries.map {
                            StatusOption(stringResource(it.toDisplayLabelRes()), it.toColor())
                        }
                        StatusSelectionSheet(
                            title = stringResource(R.string.anime_detail_add_sheet_title),
                            subtitle = uiState.pendingAddSeason?.let {
                                resolveDisplayTitle(
                                    title = it.title,
                                    titleEnglish = it.titleEnglish,
                                    titleJapanese = it.titleJapanese,
                                    language = uiState.titleLanguage
                                )
                            } ?: animeDisplayTitle,
                            options = statusOptions,
                            onOptionSelected = { index ->
                                onAddSeasonStatusSelected(WatchStatus.entries[index])
                            },
                            onDismiss = onDismissAddSeasonSheet
                        )
                    }

                    if (uiState.isNotificationTypeSheetVisible) {
                        val notificationOptions = listOf(
                            stringResource(R.string.anime_detail_notification_new_episodes),
                            stringResource(R.string.anime_detail_notification_new_seasons),
                            stringResource(R.string.anime_detail_notification_both)
                        )
                        val notificationTypes = listOf(
                            NotificationType.NEW_EPISODES,
                            NotificationType.NEW_SEASONS,
                            NotificationType.BOTH
                        )
                        OptionSelectionSheet(
                            title = stringResource(R.string.anime_detail_notification_sheet_title),
                            subtitle = animeDisplayTitle,
                            options = notificationOptions,
                            onOptionSelected = { index ->
                                onSelectNotificationType(notificationTypes[index])
                            },
                            onDismiss = onDismissNotificationTypeSheet
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
private fun AnimeDetailContent(
    state: AnimeDetailUiState.Success,
    onStatusChipClick: () -> Unit,
    onRatingChanged: (Int) -> Unit,
    onAddToWatchlistClick: () -> Unit,
    onSeasonClick: (seasonId: Long, malId: Int) -> Unit,
    onSeasonAddClick: (Season) -> Unit
) {
    val anime = state.anime
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item(key = "header") {
            AnimeHeaderSection(
                anime = anime,
                titleLanguage = state.titleLanguage,
                isInWatchlist = state.isInWatchlist,
                onStatusChipClick = onStatusChipClick,
                onAddToWatchlistClick = onAddToWatchlistClick
            )
        }

        if (state.isInWatchlist) {
            item(key = "rating") {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.anime_detail_section_your_rating),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                RatingBar(
                    rating = anime.userRating ?: 0,
                    isInteractive = true,
                    onRatingChanged = onRatingChanged
                )
            }
        }

        val synopsis = anime.synopsis
        if (synopsis != null) {
            item(key = "synopsis") {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.anime_detail_section_synopsis),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = synopsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (state.seasons.isNotEmpty()) {
            item(key = "seasons_header") {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.anime_detail_section_seasons),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(
                items = state.seasons,
                key = { it.id.takeIf { id -> id > 0 } ?: it.malId.toLong() }
            ) { season ->
                SeasonCardItem(
                    season = season,
                    titleLanguage = state.titleLanguage,
                    onClick = { onSeasonClick(season.id, season.malId) },
                    onAddClick = { onSeasonAddClick(season) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AnimeHeaderSection(
    anime: Anime,
    titleLanguage: TitleLanguage,
    isInWatchlist: Boolean,
    onStatusChipClick: () -> Unit,
    onAddToWatchlistClick: () -> Unit
) {
    val displayTitle = resolveDisplayTitle(
        title = anime.title,
        titleEnglish = anime.titleEnglish,
        titleJapanese = anime.titleJapanese,
        language = titleLanguage
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = anime.imageUrl,
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

        Column {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isInWatchlist) {
                StatusChip(
                    label = stringResource(anime.status.toDisplayLabelRes()),
                    color = anime.status.toColor(),
                    modifier = Modifier.clickable(onClick = onStatusChipClick)
                )
            } else {
                Button(onClick = onAddToWatchlistClick) {
                    Text(stringResource(R.string.anime_detail_add_to_watchlist))
                }
            }

            if (anime.genres.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = anime.genres.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun resolveSnackbarMessage(event: AnimeDetailSnackbarEvent): String = when (event) {
    is AnimeDetailSnackbarEvent.AddedToWatchlist ->
        stringResource(R.string.anime_detail_added_to_watchlist, event.title)
    is AnimeDetailSnackbarEvent.NotificationsEnabled -> when (event.type) {
        NotificationType.NEW_EPISODES -> stringResource(R.string.anime_detail_notifications_enabled_episodes)
        NotificationType.NEW_SEASONS -> stringResource(R.string.anime_detail_notifications_enabled_seasons)
        NotificationType.BOTH -> stringResource(R.string.anime_detail_notifications_enabled_both)
        NotificationType.NONE -> stringResource(R.string.anime_detail_notifications_disabled)
    }
    is AnimeDetailSnackbarEvent.NotificationsDisabled ->
        stringResource(R.string.anime_detail_notifications_disabled)
    is AnimeDetailSnackbarEvent.NotificationPermissionDenied ->
        stringResource(R.string.notification_permission_denied)
}

@Composable
private fun SeasonCardItem(
    season: Season,
    titleLanguage: TitleLanguage,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val displayTitle = resolveDisplayTitle(
        title = season.title,
        titleEnglish = season.titleEnglish,
        titleJapanese = season.titleJapanese,
        language = titleLanguage
    )
    val totalEpisodes = season.episodeCount
    val episodeText = if (season.isInWatchlist && totalEpisodes != null) {
        stringResource(R.string.anime_detail_season_episodes, season.currentEpisode, totalEpisodes)
    } else if (season.isInWatchlist && season.currentEpisode > 0) {
        stringResource(R.string.anime_detail_season_episodes_no_total, season.currentEpisode)
    } else {
        totalEpisodes?.let { stringResource(R.string.season_detail_episode_count, it) }
    }

    val progress = if (season.isInWatchlist && totalEpisodes != null && totalEpisodes > 0) {
        season.currentEpisode.toFloat() / totalEpisodes.toFloat()
    } else {
        null
    }

    AnimeCard(
        title = displayTitle,
        imageUrl = season.imageUrl,
        onClick = onClick,
        score = season.score,
        episodeText = episodeText,
        genresText = "${season.type}${season.airingStatus?.let { " · $it" } ?: ""}",
        progress = progress,
        trailingContent = if (season.isInWatchlist) {
            {
                StatusChip(
                    label = stringResource(season.status.toDisplayLabelRes()),
                    color = season.status.toColor()
                )
            }
        } else {
            {
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.anime_detail_add_to_watchlist)
                    )
                }
            }
        }
    )
}
