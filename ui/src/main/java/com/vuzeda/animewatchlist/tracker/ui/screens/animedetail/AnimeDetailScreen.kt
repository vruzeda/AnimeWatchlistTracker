package com.vuzeda.animewatchlist.tracker.ui.screens.animedetail

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
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
import com.vuzeda.animewatchlist.tracker.designsystem.component.AnimeCard
import com.vuzeda.animewatchlist.tracker.designsystem.component.ConfirmationDialog
import com.vuzeda.animewatchlist.tracker.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.designsystem.component.RatingBar
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusChip
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusOption
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusSelectionSheet
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import com.vuzeda.animewatchlist.tracker.domain.model.resolveDisplayTitle
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.R
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toColor
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toDisplayLabelRes

@Composable
fun AnimeDetailScreenRoute(
    onNavigateBack: () -> Unit,
    onSeasonClick: (seasonId: Long, malId: Int) -> Unit,
    viewModel: AnimeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState is AnimeDetailUiState.Success && (uiState as AnimeDetailUiState.Success).isDeleted) {
        LaunchedEffect(Unit) { onNavigateBack() }
    }

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
        onToggleNotifications = viewModel::toggleNotifications,
        onAddToWatchlistClick = viewModel::showAddSheet,
        onAddStatusSelected = viewModel::addToWatchlist,
        onDismissAddSheet = viewModel::dismissAddSheet,
        onSnackbarDismissed = viewModel::clearSnackbar
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
    onToggleNotifications: () -> Unit,
    onAddToWatchlistClick: () -> Unit,
    onAddStatusSelected: (WatchStatus) -> Unit,
    onDismissAddSheet: () -> Unit,
    onSnackbarDismissed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState is AnimeDetailUiState.Success && uiState.snackbarMessage != null) {
        LaunchedEffect(uiState.snackbarMessage) {
            snackbarHostState.showSnackbar(uiState.snackbarMessage)
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
                        onSeasonClick = onSeasonClick
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
                                onDismissAddSheet()
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
private fun AnimeDetailContent(
    state: AnimeDetailUiState.Success,
    onStatusChipClick: () -> Unit,
    onRatingChanged: (Int) -> Unit,
    onAddToWatchlistClick: () -> Unit,
    onSeasonClick: (seasonId: Long, malId: Int) -> Unit
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
                    isInWatchlist = state.isInWatchlist,
                    onClick = { onSeasonClick(season.id, season.malId) }
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
private fun SeasonCardItem(
    season: Season,
    titleLanguage: TitleLanguage,
    isInWatchlist: Boolean,
    onClick: () -> Unit
) {
    val displayTitle = resolveDisplayTitle(
        title = season.title,
        titleEnglish = season.titleEnglish,
        titleJapanese = season.titleJapanese,
        language = titleLanguage
    )
    val totalEpisodes = season.episodeCount
    val episodeText = if (isInWatchlist && totalEpisodes != null) {
        stringResource(R.string.anime_detail_season_episodes, season.currentEpisode, totalEpisodes)
    } else if (isInWatchlist && season.currentEpisode > 0) {
        stringResource(R.string.anime_detail_season_episodes_no_total, season.currentEpisode)
    } else {
        totalEpisodes?.let { stringResource(R.string.season_detail_episode_count, it) }
    }

    val progress = if (isInWatchlist && totalEpisodes != null && totalEpisodes > 0) {
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
        progress = progress
    )
}
