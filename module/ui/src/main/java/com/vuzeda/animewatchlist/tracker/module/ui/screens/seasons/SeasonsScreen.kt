package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.AnimeCard
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.ConfirmationDialog
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.SeasonPickerRow
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.SortMenuButton
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.StatusOption
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.StatusSelectionSheet
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.resolveDisplayTitle
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.ui.R
import com.vuzeda.animewatchlist.tracker.module.ui.screens.home.toColor
import com.vuzeda.animewatchlist.tracker.module.ui.screens.home.toDisplayLabelRes

@Composable
fun SeasonsScreenRoute(
    onNavigateToDetailByMalId: (Int) -> Unit,
    viewModel: SeasonsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.pendingNavigationMalId) {
        val malId = uiState.pendingNavigationMalId
        if (malId != null) {
            viewModel.onNavigated()
            onNavigateToDetailByMalId(malId)
        }
    }

    SeasonsScreen(
        uiState = uiState,
        onPreviousSeason = viewModel::selectPreviousSeason,
        onNextSeason = viewModel::selectNextSeason,
        onSortSelected = viewModel::selectSort,
        onResultClick = viewModel::onResultClick,
        onAddClick = viewModel::onAddClick,
        onRemoveClick = viewModel::onRemoveClick,
        onAddStatusSelected = viewModel::addToWatchlist,
        onDismissAddSheet = viewModel::dismissBottomSheet,
        onConfirmRemove = viewModel::confirmRemoveFromWatchlist,
        onDismissRemoveConfirmation = viewModel::dismissDeleteConfirmation,
        onLoadMore = viewModel::loadMore,
        onSnackbarDismissed = viewModel::clearSnackbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonsScreen(
    uiState: SeasonsUiState,
    onPreviousSeason: () -> Unit,
    onNextSeason: () -> Unit,
    onSortSelected: (SeasonsSortOption) -> Unit,
    onResultClick: (SearchResult) -> Unit,
    onAddClick: (SearchResult) -> Unit,
    onRemoveClick: (SearchResult) -> Unit,
    onAddStatusSelected: (WatchStatus) -> Unit,
    onDismissAddSheet: () -> Unit,
    onConfirmRemove: () -> Unit,
    onDismissRemoveConfirmation: () -> Unit,
    onLoadMore: () -> Unit,
    onSnackbarDismissed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val addedFormat = stringResource(R.string.seasons_added_to_watchlist, uiState.snackbarMessage ?: "")
    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            snackbarHostState.showSnackbar(addedFormat)
            onSnackbarDismissed()
        }
    }

    val seasonLabel = seasonDisplayLabel(uiState.selectedSeason)
    val pickerLabel = stringResource(R.string.seasons_picker_label, seasonLabel, uiState.selectedYear)
    val sortOptions = SeasonsSortOption.entries.map { stringResource(it.displayLabelRes) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.seasons_title)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    if (uiState.animeList.isNotEmpty()) {
                        SortMenuButton(
                            options = sortOptions,
                            selectedIndex = uiState.sortOption.ordinal,
                            isAscending = uiState.isSortAscending,
                            onOptionSelected = { index -> onSortSelected(SeasonsSortOption.entries[index]) }
                        )
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
            SeasonPickerRow(
                modifier = Modifier.padding(horizontal = 8.dp),
                label = pickerLabel,
                isNextEnabled = uiState.isNextSeasonEnabled,
                onPreviousClick = onPreviousSeason,
                onNextClick = onNextSeason
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.seasons_error_title),
                        subtitle = uiState.errorMessage
                    )
                }
                uiState.animeList.isEmpty() -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.seasons_empty_title),
                        subtitle = stringResource(R.string.seasons_empty_subtitle)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.displayedAnimeList,
                            key = { it.malId }
                        ) { result ->
                            val isAdded = result.malId in uiState.addedMalIds
                            val isResolving = uiState.resolvingMalId == result.malId
                            val displayTitle = resolveDisplayTitle(
                                title = result.title,
                                titleEnglish = result.titleEnglish,
                                titleJapanese = result.titleJapanese,
                                language = uiState.titleLanguage
                            )
                            AnimeCard(
                                title = displayTitle,
                                imageUrl = result.imageUrl,
                                onClick = { onResultClick(result) },
                                score = result.score,
                                episodeText = result.episodeCount?.let {
                                    stringResource(R.string.seasons_episode_count, it)
                                },
                                genresText = result.genres.takeIf { it.isNotEmpty() }?.joinToString(", "),
                                trailingContent = {
                                    if (isResolving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else if (isAdded) {
                                        IconButton(onClick = { onRemoveClick(result) }) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = stringResource(R.string.cd_already_in_watchlist),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else {
                                        IconButton(onClick = { onAddClick(result) }) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = stringResource(R.string.cd_add_to_watchlist),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        if (uiState.hasNextPage) {
                            item(key = "load_more") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isLoadingMore) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    } else {
                                        OutlinedButton(onClick = onLoadMore) {
                                            Text(stringResource(R.string.seasons_load_more))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.selectedResultForDelete != null) {
                        ConfirmationDialog(
                            title = stringResource(R.string.delete_anime_dialog_title),
                            message = stringResource(R.string.delete_anime_dialog_message),
                            confirmText = stringResource(R.string.delete_anime_dialog_confirm),
                            dismissText = stringResource(R.string.delete_anime_dialog_dismiss),
                            onConfirm = onConfirmRemove,
                            onDismiss = onDismissRemoveConfirmation
                        )
                    }

                    if (uiState.selectedResultForAdd != null) {
                        val statusOptions = WatchStatus.entries.map {
                            StatusOption(stringResource(it.toDisplayLabelRes()), it.toColor())
                        }
                        val sheetSubtitle = resolveDisplayTitle(
                            title = uiState.selectedResultForAdd.title,
                            titleEnglish = uiState.selectedResultForAdd.titleEnglish,
                            titleJapanese = uiState.selectedResultForAdd.titleJapanese,
                            language = uiState.titleLanguage
                        )
                        StatusSelectionSheet(
                            title = stringResource(R.string.seasons_add_sheet_title),
                            subtitle = sheetSubtitle,
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
}

@Composable
private fun seasonDisplayLabel(season: AnimeSeason): String = when (season) {
    AnimeSeason.WINTER -> stringResource(R.string.seasons_label_winter)
    AnimeSeason.SPRING -> stringResource(R.string.seasons_label_spring)
    AnimeSeason.SUMMER -> stringResource(R.string.seasons_label_summer)
    AnimeSeason.FALL -> stringResource(R.string.seasons_label_fall)
}
