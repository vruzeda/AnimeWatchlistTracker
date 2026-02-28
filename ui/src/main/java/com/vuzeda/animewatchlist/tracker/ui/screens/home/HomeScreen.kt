package com.vuzeda.animewatchlist.tracker.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vuzeda.animewatchlist.tracker.designsystem.component.AnimeCard
import com.vuzeda.animewatchlist.tracker.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusCompleted
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusDropped
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusOnHold
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusPlanToWatch
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusWatching
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

@Composable
fun HomeScreenRoute(
    onAnimeClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onTabSelected = viewModel::selectTab,
        onAnimeClick = onAnimeClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTabSelected: (WatchStatus?) -> Unit,
    onAnimeClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("My Watchlist") })

        val tabs = listOf<Pair<String, WatchStatus?>>(
            "All" to null,
            "Watching" to WatchStatus.WATCHING,
            "Completed" to WatchStatus.COMPLETED,
            "Plan to Watch" to WatchStatus.PLAN_TO_WATCH,
            "On Hold" to WatchStatus.ON_HOLD,
            "Dropped" to WatchStatus.DROPPED
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
                    title = "No anime here yet",
                    subtitle = "Search for anime to add to your watchlist"
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
                                "${anime.currentEpisode} / ${anime.episodeCount} ep"
                            } else {
                                "${anime.currentEpisode} ep"
                            }
                            val progress = anime.episodeCount?.takeIf { it > 0 }?.let {
                                (anime.currentEpisode.toFloat() / it).coerceIn(0f, 1f)
                            }
                            AnimeCard(
                                title = anime.title,
                                imageUrl = anime.imageUrl,
                                onClick = { onAnimeClick(anime.id) },
                                statusLabel = anime.status.toDisplayLabel(),
                                statusColor = anime.status.toColor(),
                                score = anime.score,
                                episodeText = episodeText,
                                progress = progress
                            )
                    }
                }
            }
        }
    }
}

fun WatchStatus.toDisplayLabel(): String = when (this) {
    WatchStatus.WATCHING -> "Watching"
    WatchStatus.COMPLETED -> "Completed"
    WatchStatus.PLAN_TO_WATCH -> "Plan to Watch"
    WatchStatus.ON_HOLD -> "On Hold"
    WatchStatus.DROPPED -> "Dropped"
}

fun WatchStatus.toColor(): Color = when (this) {
    WatchStatus.WATCHING -> StatusWatching
    WatchStatus.COMPLETED -> StatusCompleted
    WatchStatus.PLAN_TO_WATCH -> StatusPlanToWatch
    WatchStatus.ON_HOLD -> StatusOnHold
    WatchStatus.DROPPED -> StatusDropped
}
