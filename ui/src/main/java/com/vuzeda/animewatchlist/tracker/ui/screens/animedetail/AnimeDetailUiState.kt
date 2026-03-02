package com.vuzeda.animewatchlist.tracker.ui.screens.animedetail

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.NotificationType
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

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
        val isNotificationTypeSheetVisible: Boolean = false,
        val isDeleteConfirmationVisible: Boolean = false,
        val isDeleted: Boolean = false,
        val snackbarEvent: AnimeDetailSnackbarEvent? = null
    ) : AnimeDetailUiState {
        val isNotificationsEnabled: Boolean get() = notificationType != NotificationType.NONE
    }
}

sealed interface AnimeDetailSnackbarEvent {
    data class AddedToWatchlist(val title: String) : AnimeDetailSnackbarEvent
    data class NotificationsEnabled(val type: NotificationType) : AnimeDetailSnackbarEvent
    data object NotificationsDisabled : AnimeDetailSnackbarEvent
}
