package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage

data class LocalBroadcastTime(
    val day: String,
    val time: String,
    val zone: String
)

data class SeasonDetailUiState(
    val isLoading: Boolean = true,
    val isNotFound: Boolean = false,
    val season: Season? = null,
    val isInWatchlist: Boolean = true,
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val episodes: List<EpisodeInfo> = emptyList(),
    val isLoadingEpisodes: Boolean = false,
    val hasMoreEpisodes: Boolean = false,
    val nextEpisodePage: Int = 1,
    val isLastSeason: Boolean = false,
    val isStatusSheetVisible: Boolean = false,
    val isDeleteConfirmationVisible: Boolean = false,
    val isAddSheetVisible: Boolean = false,
    val isEpisodeNotificationsEnabled: Boolean = false,
    val broadcastLocalTime: LocalBroadcastTime? = null,
    val snackbarEvent: SeasonDetailSnackbarEvent? = null,
    val pendingNavigationMalId: Int? = null,
    val isRefreshing: Boolean = false,
    val isNotificationDebugInfoEnabled: Boolean = false,
    val watchedEpisodes: Set<Int> = emptySet()
)

sealed interface SeasonDetailSnackbarEvent {
    data class AddedToWatchlist(val title: String) : SeasonDetailSnackbarEvent
    data class EpisodeNotificationsToggled(val enabled: Boolean) : SeasonDetailSnackbarEvent
    data object NotificationPermissionDenied : SeasonDetailSnackbarEvent
}
