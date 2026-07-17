package com.vuzeda.animewatchlist.tracker.module.ui.screens.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.AnimeCard
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.SeasonPickerRow
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ScreenPadding
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SmallSpacing
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeDayOfWeek
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.resolveAnimeDisplayTitle
import com.vuzeda.animewatchlist.tracker.module.ui.R
import com.vuzeda.animewatchlist.tracker.module.ui.screens.ScreenPreviewSamples

@Composable
fun ScheduleScreenRoute(
    onNavigateBack: () -> Unit,
    onSeasonClick: (Long) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScheduleScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onPreviousSeason = viewModel::onPreviousSeason,
        onNextSeason = viewModel::onNextSeason,
        onSeasonClick = onSeasonClick,
        onRefresh = viewModel::refresh,
        onRetryClick = viewModel::retry,
        onSnackbarDismissed = viewModel::clearSnackbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    uiState: ScheduleUiState,
    onNavigateBack: () -> Unit,
    onPreviousSeason: () -> Unit,
    onNextSeason: () -> Unit,
    onSeasonClick: (Long) -> Unit,
    onRefresh: () -> Unit = {},
    onRetryClick: () -> Unit = {},
    onSnackbarDismissed: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState.snackbarEvent != null) {
        val message = stringResource(R.string.home_checking_for_updates)
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
                title = { Text(stringResource(R.string.schedule_title)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
            val seasonLabel = seasonDisplayLabel(uiState.selectedSeason)
            val pickerLabel = stringResource(R.string.seasons_picker_label, seasonLabel, uiState.selectedYear)

            SeasonPickerRow(
                modifier = Modifier.padding(horizontal = ScreenPadding),
                label = pickerLabel,
                onPreviousClick = onPreviousSeason,
                onNextClick = onNextSeason
            )

            HorizontalDivider()

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.hasLoadFailed -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.schedule_load_failed),
                        onRetry = onRetryClick
                    )
                }
                uiState.schedule.isEmpty() -> {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        EmptyStateMessage(
                            modifier = Modifier.fillMaxSize(),
                            title = stringResource(R.string.schedule_season_no_data),
                            subtitle = stringResource(R.string.schedule_empty_subtitle)
                        )
                    }
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = ElementSpacing),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            uiState.schedule.forEach { (dayOfWeek, seasons) ->
                                item(key = dayOfWeek) {
                                    Text(
                                        text = dayOfWeekLabel(dayOfWeek),
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = ScreenPadding, end = ScreenPadding, top = SmallSpacing, bottom = SmallSpacing)
                                    )
                                }
                                items(items = seasons, key = { it.id }) { season ->
                                    ScheduleSeasonCard(
                                        season = season,
                                        titleLanguage = uiState.titleLanguage,
                                        onSeasonClick = onSeasonClick,
                                        modifier = Modifier.padding(horizontal = ScreenPadding, vertical = SmallSpacing)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleSeasonCard(
    season: Season,
    titleLanguage: com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage,
    onSeasonClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayTitle = resolveAnimeDisplayTitle(
        titleRomaji = season.title,
        titleEnglish = season.titleEnglish,
        titleJapanese = season.titleJapanese,
        language = titleLanguage
    )
    AnimeCard(
        modifier = modifier,
        title = displayTitle,
        imageUrl = season.imageUrl,
        onClick = { onSeasonClick(season.id) },
        imageSharedElementKey = "season_cover_${season.malId}",
        episodeText = season.broadcastTime
    )
}

@Composable
private fun seasonDisplayLabel(season: AnimeSeason): String = when (season) {
    AnimeSeason.WINTER -> stringResource(R.string.seasons_label_winter)
    AnimeSeason.SPRING -> stringResource(R.string.seasons_label_spring)
    AnimeSeason.SUMMER -> stringResource(R.string.seasons_label_summer)
    AnimeSeason.FALL -> stringResource(R.string.seasons_label_fall)
}

@Composable
private fun dayOfWeekLabel(dayOfWeek: AnimeDayOfWeek): String = when (dayOfWeek) {
    AnimeDayOfWeek.MONDAY -> stringResource(R.string.schedule_day_monday)
    AnimeDayOfWeek.TUESDAY -> stringResource(R.string.schedule_day_tuesday)
    AnimeDayOfWeek.WEDNESDAY -> stringResource(R.string.schedule_day_wednesday)
    AnimeDayOfWeek.THURSDAY -> stringResource(R.string.schedule_day_thursday)
    AnimeDayOfWeek.FRIDAY -> stringResource(R.string.schedule_day_friday)
    AnimeDayOfWeek.SATURDAY -> stringResource(R.string.schedule_day_saturday)
    AnimeDayOfWeek.SUNDAY -> stringResource(R.string.schedule_day_sunday)
    AnimeDayOfWeek.UNKNOWN -> stringResource(R.string.schedule_day_unknown)
}

@Preview(showBackground = true)
@Composable
private fun ScheduleScreenPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        ScheduleScreen(
            uiState = ScreenPreviewSamples.scheduleUiState,
            onNavigateBack = {},
            onPreviousSeason = {},
            onNextSeason = {},
            onSeasonClick = {}
        )
    }
}
