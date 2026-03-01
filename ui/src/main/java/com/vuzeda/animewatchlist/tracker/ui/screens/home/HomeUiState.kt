package com.vuzeda.animewatchlist.tracker.ui.screens.home

import androidx.annotation.StringRes
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.R

enum class HomeSortOption(@param:StringRes val displayLabelRes: Int, val defaultAscending: Boolean) {
    ALPHABETICAL(R.string.sort_alphabetical, true),
    MAL_SCORE(R.string.sort_mal_score, false),
    PROGRESS(R.string.sort_progress, false),
    RECENTLY_ADDED(R.string.sort_recently_added, false),
    USER_RATING(R.string.sort_user_rating, false)
}

data class HomeUiState(
    val animeList: List<Anime> = emptyList(),
    val selectedTab: WatchStatus? = null,
    val sortOption: HomeSortOption = HomeSortOption.ALPHABETICAL,
    val isSortAscending: Boolean = HomeSortOption.ALPHABETICAL.defaultAscending,
    val isLoading: Boolean = true
)
