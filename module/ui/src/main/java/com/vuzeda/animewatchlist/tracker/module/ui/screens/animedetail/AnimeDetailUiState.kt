package com.vuzeda.animewatchlist.tracker.module.ui.screens.animedetail

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus

sealed interface AnimeDetailUiState {
    data object Loading : AnimeDetailUiState
    data object NotFound : AnimeDetailUiState
    data class Success(
        val anime: Anime,
        val seasons: List<Season> = emptyList(),
        val isInWatchlist: Boolean = true,
        val notificationType: NotificationType = anime.notificationType,
        val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
        val isStatusSheetVisible: Boolean = false,
        val isAddSheetVisible: Boolean = false,
        val isAddScopeSheetVisible: Boolean = false,
        val pendingAddStatus: WatchStatus? = null,
        val isAddSeasonSheetVisible: Boolean = false,
        val pendingAddSeason: Season? = null,
        val isNotificationTypeSheetVisible: Boolean = false,
        val isDeleteConfirmationVisible: Boolean = false,
        val snackbarEvent: AnimeDetailSnackbarEvent? = null,
        val isRefreshing: Boolean = false,
        val isNotificationDebugInfoEnabled: Boolean = false
    ) : AnimeDetailUiState {
        val isNotificationsEnabled: Boolean get() = notificationType != NotificationType.NONE
    }
}

sealed interface AnimeDetailSnackbarEvent {
    data class AddedToWatchlist(val title: String) : AnimeDetailSnackbarEvent
    data class NotificationsEnabled(val type: NotificationType) : AnimeDetailSnackbarEvent
    data object NotificationsDisabled : AnimeDetailSnackbarEvent
    data object NotificationPermissionDenied : AnimeDetailSnackbarEvent
}
