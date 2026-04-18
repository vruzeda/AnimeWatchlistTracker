package com.vuzeda.animewatchlist.tracker.module.ui.screens.schedule

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import java.time.DayOfWeek

data class ScheduleUiState(
    val selectedYear: Int = 0,
    val selectedSeason: AnimeSeason = AnimeSeason.WINTER,
    val schedule: Map<DayOfWeek, List<Season>> = emptyMap(),
    val availableSeasons: List<Pair<Int, AnimeSeason>> = emptyList(),
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val isLoading: Boolean = true
)
