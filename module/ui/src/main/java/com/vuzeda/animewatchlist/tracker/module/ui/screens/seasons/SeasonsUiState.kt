package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasons

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonsSortOption
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonsSortState
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.ui.R

val SeasonsSortOption.displayLabelRes: Int
    get() = when (this) {
        SeasonsSortOption.DEFAULT -> R.string.seasons_sort_default
        SeasonsSortOption.ALPHABETICAL -> R.string.sort_alphabetical
        SeasonsSortOption.SCORE -> R.string.sort_score
    }

data class SeasonsDisplayData(
    val displayedAnimeList: List<SearchResult>,
    val sortState: SeasonsSortState,
    val titleLanguage: TitleLanguage,
    val addedMalIds: Set<Int>
)

data class SeasonsUiState(
    val selectedYear: Int = 0,
    val selectedSeason: AnimeSeason = AnimeSeason.WINTER,
    val currentYear: Int = 0,
    val currentSeason: AnimeSeason = AnimeSeason.WINTER,
    val animeList: List<SearchResult> = emptyList(),
    val displayedAnimeList: List<SearchResult> = emptyList(),
    val sortOption: SeasonsSortOption = SeasonsSortOption.DEFAULT,
    val isSortAscending: Boolean = SeasonsSortOption.DEFAULT.defaultAscending,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasNextPage: Boolean = false,
    val currentPage: Int = 1,
    val errorMessage: String? = null,
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val selectedResultForAdd: SearchResult? = null,
    val selectedResultForDelete: SearchResult? = null,
    val snackbarMessage: String? = null,
    val pendingNavigationMalId: Int? = null,
    val addedMalIds: Set<Int> = emptySet(),
    val resolvingMalId: Int? = null,
    val isRefreshing: Boolean = false
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
