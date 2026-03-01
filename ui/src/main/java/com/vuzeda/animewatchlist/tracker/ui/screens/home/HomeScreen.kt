package com.vuzeda.animewatchlist.tracker.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vuzeda.animewatchlist.tracker.designsystem.component.AnimeCard
import com.vuzeda.animewatchlist.tracker.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.designsystem.component.FilterMenuButton
import com.vuzeda.animewatchlist.tracker.designsystem.component.SortMenuButton
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusChip
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusCompleted
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.R
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusDropped
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusOnHold
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusPlanToWatch
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusWatching

@Composable
fun HomeScreenRoute(
    onAnimeClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onFilterSelected = viewModel::selectFilter,
        onSortSelected = viewModel::selectSort,
        onAnimeClick = onAnimeClick
    )
}

private fun buildFilterOptions(
    allLabel: String,
    watchingLabel: String,
    completedLabel: String,
    planToWatchLabel: String,
    onHoldLabel: String,
    droppedLabel: String,
    notificationsOnLabel: String,
    notificationsOffLabel: String
): List<Pair<String, HomeFilter>> = listOf(
    allLabel to HomeFilter.All,
    watchingLabel to HomeFilter.ByStatus(WatchStatus.WATCHING),
    completedLabel to HomeFilter.ByStatus(WatchStatus.COMPLETED),
    planToWatchLabel to HomeFilter.ByStatus(WatchStatus.PLAN_TO_WATCH),
    onHoldLabel to HomeFilter.ByStatus(WatchStatus.ON_HOLD),
    droppedLabel to HomeFilter.ByStatus(WatchStatus.DROPPED),
    notificationsOnLabel to HomeFilter.NotificationsOn,
    notificationsOffLabel to HomeFilter.NotificationsOff
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onFilterSelected: (HomeFilter) -> Unit,
    onSortSelected: (HomeSortOption) -> Unit,
    onAnimeClick: (Long) -> Unit
) {
    val sortOptions = HomeSortOption.entries.map { stringResource(it.displayLabelRes) }

    val filterOptions = buildFilterOptions(
        allLabel = stringResource(R.string.home_tab_all),
        watchingLabel = stringResource(R.string.status_watching),
        completedLabel = stringResource(R.string.status_completed),
        planToWatchLabel = stringResource(R.string.status_plan_to_watch),
        onHoldLabel = stringResource(R.string.status_on_hold),
        droppedLabel = stringResource(R.string.status_dropped),
        notificationsOnLabel = stringResource(R.string.filter_notifications_on),
        notificationsOffLabel = stringResource(R.string.filter_notifications_off)
    )
    val selectedFilterIndex = filterOptions.indexOfFirst { it.second == uiState.selectedFilter }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.home_title)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            actions = {
                FilterMenuButton(
                    options = filterOptions.map { it.first },
                    selectedIndex = selectedFilterIndex,
                    onOptionSelected = { index -> onFilterSelected(filterOptions[index].second) }
                )
                SortMenuButton(
                    options = sortOptions,
                    selectedIndex = uiState.sortOption.ordinal,
                    isAscending = uiState.isSortAscending,
                    onOptionSelected = { index -> onSortSelected(HomeSortOption.entries[index]) }
                )
            }
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
            uiState.animeList.isEmpty() -> {
                EmptyStateMessage(
                    modifier = Modifier.fillMaxSize(),
                    title = stringResource(R.string.home_empty_title),
                    subtitle = stringResource(R.string.home_empty_subtitle)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.animeList,
                        key = { it.id }
                    ) { anime ->
                        AnimeCard(
                            title = anime.title,
                            imageUrl = anime.imageUrl,
                            onClick = { onAnimeClick(anime.id) },
                            genresText = anime.genres.takeIf { it.isNotEmpty() }?.joinToString(", "),
                            trailingContent = {
                                StatusChip(
                                    label = stringResource(anime.status.toDisplayLabelRes()),
                                    color = anime.status.toColor()
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

fun WatchStatus.toDisplayLabelRes(): Int = when (this) {
    WatchStatus.WATCHING -> R.string.status_watching
    WatchStatus.COMPLETED -> R.string.status_completed
    WatchStatus.PLAN_TO_WATCH -> R.string.status_plan_to_watch
    WatchStatus.ON_HOLD -> R.string.status_on_hold
    WatchStatus.DROPPED -> R.string.status_dropped
}

fun WatchStatus.toColor(): Color = when (this) {
    WatchStatus.WATCHING -> StatusWatching
    WatchStatus.COMPLETED -> StatusCompleted
    WatchStatus.PLAN_TO_WATCH -> StatusPlanToWatch
    WatchStatus.ON_HOLD -> StatusOnHold
    WatchStatus.DROPPED -> StatusDropped
}
