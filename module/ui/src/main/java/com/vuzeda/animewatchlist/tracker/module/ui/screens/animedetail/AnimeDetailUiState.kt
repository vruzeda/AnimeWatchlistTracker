package com.vuzeda.animewatchlist.tracker.module.ui.screens.animedetail

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus

data class AnimeDetailUiState(
    val isLoading: Boolean = true,
    val isNotFound: Boolean = false,
    val anime: Anime? = null,
    val seasons: List<Season> = emptyList(),
    val isInWatchlist: Boolean = true,
    val notificationType: NotificationType = NotificationType.NONE,
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val isAddSheetVisible: Boolean = false,
    val isAddScopeSheetVisible: Boolean = false,
    val pendingAddStatus: WatchStatus? = null,
    val isAddSeasonSheetVisible: Boolean = false,
    val pendingAddSeason: Season? = null,
    val isNotificationTypeSheetVisible: Boolean = false,
    val isDeleteConfirmationVisible: Boolean = false,
    val snackbarEvent: AnimeDetailSnackbarEvent? = null,
    val isRefreshing: Boolean = false,
    val isNotificationDebugInfoEnabled: Boolean = false,
    val typeFilter: Set<String> = emptySet()
) {
    val isNotificationsEnabled: Boolean get() = notificationType != NotificationType.NONE
}

sealed interface AnimeDetailSnackbarEvent {
    data class AddedToWatchlist(val title: String) : AnimeDetailSnackbarEvent
    data class NotificationsEnabled(val type: NotificationType) : AnimeDetailSnackbarEvent
    data object NotificationsDisabled : AnimeDetailSnackbarEvent
    data object NotificationPermissionDenied : AnimeDetailSnackbarEvent
}
