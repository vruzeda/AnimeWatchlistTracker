package com.vuzeda.animewatchlist.tracker.ui.screens.home

import androidx.annotation.StringRes
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.R

sealed interface HomeFilter {
    data object All : HomeFilter
    data class ByStatus(val status: WatchStatus) : HomeFilter
    data object NotificationsOn : HomeFilter
    data object NotificationsOff : HomeFilter
}

enum class HomeSortOption(@param:StringRes val displayLabelRes: Int, val defaultAscending: Boolean) {
    ALPHABETICAL(R.string.sort_alphabetical, true),
    RECENTLY_ADDED(R.string.sort_recently_added, false),
    USER_RATING(R.string.sort_user_rating, false)
}

data class HomeUiState(
    val animeList: List<Anime> = emptyList(),
    val selectedFilter: HomeFilter = HomeFilter.All,
    val sortOption: HomeSortOption = HomeSortOption.ALPHABETICAL,
    val isSortAscending: Boolean = HomeSortOption.ALPHABETICAL.defaultAscending,
    val isLoading: Boolean = true
)
