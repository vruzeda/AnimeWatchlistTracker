package com.vuzeda.animewatchlist.tracker.ui.screens.home

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

enum class HomeSortOption(val displayLabel: String) {
    ALPHABETICAL("Alphabetical"),
    RECENTLY_ADDED("Recently Added"),
    MAL_SCORE("MAL Score"),
    USER_RATING("Your Rating"),
    PROGRESS("Progress")
}

data class HomeUiState(
    val animeList: List<Anime> = emptyList(),
    val selectedTab: WatchStatus? = null,
    val sortOption: HomeSortOption = HomeSortOption.ALPHABETICAL,
    val isLoading: Boolean = true
)
