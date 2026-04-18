package com.vuzeda.animewatchlist.tracker.module.ui.screens.home

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortOption
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortState
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.ui.R

val HomeSortOption.displayLabelRes: Int
    get() = when (this) {
        HomeSortOption.ALPHABETICAL -> R.string.sort_alphabetical
        HomeSortOption.RECENTLY_ADDED -> R.string.sort_recently_added
        HomeSortOption.USER_RATING -> R.string.sort_user_rating
        HomeSortOption.WATCH_STATUS -> R.string.sort_watch_status
    }

data class HomeFilterState(
    val statusFilter: WatchStatus? = null,
    val notificationFilter: Boolean? = null
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

