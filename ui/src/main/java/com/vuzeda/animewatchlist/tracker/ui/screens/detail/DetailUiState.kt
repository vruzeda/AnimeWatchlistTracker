package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState
    data class Success(
        val anime: Anime,
        val isEditing: Boolean = false,
        val editStatus: WatchStatus = anime.status,
        val editCurrentEpisode: Int = anime.currentEpisode,
        val editUserRating: Int = anime.userRating ?: 0,
        val isNotificationsEnabled: Boolean = anime.isNotificationsEnabled
    ) : DetailUiState
}
