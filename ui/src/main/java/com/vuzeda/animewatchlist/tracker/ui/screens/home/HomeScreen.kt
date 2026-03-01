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
import com.vuzeda.animewatchlist.tracker.designsystem.component.FilterGroup
import com.vuzeda.animewatchlist.tracker.designsystem.component.NestedFilterMenuButton
import com.vuzeda.animewatchlist.tracker.designsystem.component.SortMenuButton
import com.vuzeda.animewatchlist.tracker.designsystem.component.StatusChip
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusCompleted
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.R
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusDropped
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusOnHold
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusPlanToWatch
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusWatching

private const val GROUP_STATUS = 0
private const val GROUP_NOTIFICATION = 1

private val statusValues = listOf(null) + WatchStatus.entries
private val notificationValues = listOf(null, true, false)

@Composable
fun HomeScreenRoute(
    onAnimeClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onStatusFilterSelected = viewModel::selectStatusFilter,
        onNotificationFilterSelected = viewModel::selectNotificationFilter,
        onResetFilters = viewModel::resetFilters,
        onSortSelected = viewModel::selectSort,
        onAnimeClick = onAnimeClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStatusFilterSelected: (WatchStatus?) -> Unit,
    onNotificationFilterSelected: (Boolean?) -> Unit,
    onResetFilters: () -> Unit,
    onSortSelected: (HomeSortOption) -> Unit,
    onAnimeClick: (Long) -> Unit
) {
    val sortOptions = HomeSortOption.entries.map { stringResource(it.displayLabelRes) }

    val statusFilterGroup = FilterGroup(
        label = stringResource(R.string.filter_group_status),
        options = listOf(
            stringResource(R.string.home_tab_all),
            stringResource(R.string.status_watching),
            stringResource(R.string.status_completed),
            stringResource(R.string.status_plan_to_watch),
            stringResource(R.string.status_on_hold),
            stringResource(R.string.status_dropped)
        ),
        selectedIndex = statusValues.indexOf(uiState.filterState.statusFilter)
    )

    val notificationFilterGroup = FilterGroup(
        label = stringResource(R.string.filter_group_notification),
        options = listOf(
            stringResource(R.string.home_tab_all),
            stringResource(R.string.filter_notification_on),
            stringResource(R.string.filter_notification_off)
        ),
        selectedIndex = notificationValues.indexOf(uiState.filterState.notificationFilter)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.home_title)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            actions = {
                NestedFilterMenuButton(
                    filterGroups = listOf(statusFilterGroup, notificationFilterGroup),
                    onOptionSelected = { groupIndex, optionIndex ->
                        when (groupIndex) {
                            GROUP_STATUS -> onStatusFilterSelected(statusValues[optionIndex])
                            GROUP_NOTIFICATION -> onNotificationFilterSelected(notificationValues[optionIndex])
                        }
                    },
                    resetLabel = stringResource(R.string.filter_reset),
                    onReset = onResetFilters
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
