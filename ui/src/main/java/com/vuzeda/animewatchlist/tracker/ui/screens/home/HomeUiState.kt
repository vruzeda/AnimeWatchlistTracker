package com.vuzeda.animewatchlist.tracker.ui.screens.home

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

enum class HomeSortOption(val displayLabel: String, val defaultAscending: Boolean) {
    ALPHABETICAL("Alphabetical", true),
    RECENTLY_ADDED("Recently Added", false),
    MAL_SCORE("MAL Score", false),
    USER_RATING("Your Rating", false),
    PROGRESS("Progress", false)
}

data class HomeUiState(
    val animeList: List<Anime> = emptyList(),
    val selectedTab: WatchStatus? = null,
    val sortOption: HomeSortOption = HomeSortOption.ALPHABETICAL,
    val isSortAscending: Boolean = HomeSortOption.ALPHABETICAL.defaultAscending,
    val isLoading: Boolean = true
)
