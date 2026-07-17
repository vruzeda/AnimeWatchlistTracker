package com.vuzeda.animewatchlist.tracker.module.ui.screens.schedule

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeDayOfWeek
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage

data class ScheduleUiState(
    val selectedYear: Int = 0,
    val selectedSeason: AnimeSeason = AnimeSeason.WINTER,
    val schedule: Map<AnimeDayOfWeek, List<Season>> = emptyMap(),
    val availableSeasons: List<Pair<Int, AnimeSeason>> = emptyList(),
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val isLoading: Boolean = true,
    val hasLoadFailed: Boolean = false,
    val snackbarEvent: ScheduleSnackbarEvent? = null
)

sealed interface ScheduleSnackbarEvent {
    data object UpdateCheckStarted : ScheduleSnackbarEvent
}
