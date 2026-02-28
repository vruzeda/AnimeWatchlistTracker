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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vuzeda.animewatchlist.tracker.designsystem.component.AnimeCard
import com.vuzeda.animewatchlist.tracker.designsystem.component.EmptyStateMessage
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
        onTabSelected = viewModel::selectTab,
        onSortSelected = viewModel::selectSort,
        onAnimeClick = onAnimeClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTabSelected: (WatchStatus?) -> Unit,
    onSortSelected: (HomeSortOption) -> Unit,
    onAnimeClick: (Long) -> Unit
) {
    val sortOptions = HomeSortOption.entries.map { stringResource(it.displayLabelRes) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.home_title)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            actions = {
                SortMenuButton(
                    options = sortOptions,
                    selectedIndex = uiState.sortOption.ordinal,
                    isAscending = uiState.isSortAscending,
                    onOptionSelected = { index -> onSortSelected(HomeSortOption.entries[index]) }
                )
            }
        )

        val tabs = listOf(
            stringResource(R.string.home_tab_all) to null,
            stringResource(R.string.status_watching) to WatchStatus.WATCHING,
            stringResource(R.string.status_completed) to WatchStatus.COMPLETED,
            stringResource(R.string.status_plan_to_watch) to WatchStatus.PLAN_TO_WATCH,
            stringResource(R.string.status_on_hold) to WatchStatus.ON_HOLD,
            stringResource(R.string.status_dropped) to WatchStatus.DROPPED
        )

        val selectedIndex = tabs.indexOfFirst { it.second == uiState.selectedTab }

        ScrollableTabRow(selectedTabIndex = selectedIndex) {
            tabs.forEachIndexed { index, (label, status) ->
                Tab(
                    selected = index == selectedIndex,
                    onClick = { onTabSelected(status) },
                    text = { Text(label) }
                )
            }
        }

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
                        val episodeText = if (anime.episodeCount != null) {
                            stringResource(R.string.home_episode_with_total, anime.currentEpisode, anime.episodeCount!!)
                        } else {
                            stringResource(R.string.home_episode_without_total, anime.currentEpisode)
                        }
                        val progress = anime.episodeCount?.takeIf { it > 0 }?.let {
                            (anime.currentEpisode.toFloat() / it).coerceIn(0f, 1f)
                        }
                        AnimeCard(
                            title = anime.title,
                            imageUrl = anime.imageUrl,
                            onClick = { onAnimeClick(anime.id) },
                            score = anime.score,
                            genresText = anime.genres.takeIf { it.isNotEmpty() }?.joinToString(", "),
                            episodeText = episodeText,
                            progress = progress,
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
