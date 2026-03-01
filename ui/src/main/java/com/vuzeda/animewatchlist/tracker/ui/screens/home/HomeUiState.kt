package com.vuzeda.animewatchlist.tracker.ui.screens.home

import androidx.annotation.StringRes
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.R

data class HomeFilterState(
    val statusFilter: WatchStatus? = null,
    val notificationFilter: Boolean? = null
)

enum class HomeSortOption(@param:StringRes val displayLabelRes: Int, val defaultAscending: Boolean) {
    ALPHABETICAL(R.string.sort_alphabetical, true),
    RECENTLY_ADDED(R.string.sort_recently_added, false),
    USER_RATING(R.string.sort_user_rating, false)
}

data class HomeSortState(
    val option: HomeSortOption = HomeSortOption.ALPHABETICAL,
    val isAscending: Boolean = HomeSortOption.ALPHABETICAL.defaultAscending
)

data class HomeUiState(
    val animeList: List<Anime> = emptyList(),
    val filterState: HomeFilterState = HomeFilterState(),
    val sortOption: HomeSortOption = HomeSortOption.ALPHABETICAL,
    val isSortAscending: Boolean = HomeSortOption.ALPHABETICAL.defaultAscending,
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val isLoading: Boolean = true
)
