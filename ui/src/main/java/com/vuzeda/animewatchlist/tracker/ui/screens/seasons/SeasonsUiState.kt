package com.vuzeda.animewatchlist.tracker.ui.screens.seasons

import com.vuzeda.animewatchlist.tracker.domain.model.AnimeSeason
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult

data class SeasonsUiState(
    val selectedYear: Int = 0,
    val selectedSeason: AnimeSeason = AnimeSeason.WINTER,
    val currentYear: Int = 0,
    val currentSeason: AnimeSeason = AnimeSeason.WINTER,
    val animeList: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasNextPage: Boolean = false,
    val currentPage: Int = 1,
    val errorMessage: String? = null,
    val selectedResultForAdd: SearchResult? = null,
    val snackbarMessage: String? = null,
    val pendingNavigationMalId: Int? = null,
    val addedMalIds: Set<Int> = emptySet(),
    val resolvingMalId: Int? = null
) {

    val isNextSeasonEnabled: Boolean
        get() {
            val nextSeason = selectedSeason.next()
            val nextYear = selectedYear + nextSeason.second
            val nextSeasonValue = nextSeason.first
            val currentNext = currentSeason.next()
            val upcomingYear = currentYear + currentNext.second
            val upcomingSeason = currentNext.first
            return nextYear < upcomingYear ||
                (nextYear == upcomingYear && nextSeasonValue.ordinal <= upcomingSeason.ordinal)
        }
}
