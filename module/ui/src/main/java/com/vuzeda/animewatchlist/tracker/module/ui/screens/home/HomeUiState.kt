package com.vuzeda.animewatchlist.tracker.module.ui.screens.home

import androidx.annotation.StringRes
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.ui.R

data class HomeFilterState(
    val statusFilter: WatchStatus? = null,
    val notificationFilter: Boolean? = null
)

enum class HomeSortOption(@StringRes val displayLabelRes: Int, val defaultAscending: Boolean) {
    ALPHABETICAL(R.string.sort_alphabetical, true),
    RECENTLY_ADDED(R.string.sort_recently_added, false),
    USER_RATING(R.string.sort_user_rating, false),
    WATCH_STATUS(R.string.sort_watch_status, true)
}

data class HomeSortState(
    val option: HomeSortOption = HomeSortOption.ALPHABETICAL,
    val isAscending: Boolean = HomeSortOption.ALPHABETICAL.defaultAscending
)

data class HomeSeasonItem(
    val season: Season,
    val animeStatus: WatchStatus,
    val animeNotificationType: NotificationType,
    val animeAddedAt: Long,
    val animeImageUrl: String? = null
)

data class HomeUiState(
    val homeViewMode: HomeViewMode = HomeViewMode.ANIME,
    val animeList: List<Anime> = emptyList(),
    val seasonItems: List<HomeSeasonItem> = emptyList(),
    val filterState: HomeFilterState = HomeFilterState(),
    val sortOption: HomeSortOption = HomeSortOption.ALPHABETICAL,
    val isSortAscending: Boolean = HomeSortOption.ALPHABETICAL.defaultAscending,
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val isLoading: Boolean = true
)
