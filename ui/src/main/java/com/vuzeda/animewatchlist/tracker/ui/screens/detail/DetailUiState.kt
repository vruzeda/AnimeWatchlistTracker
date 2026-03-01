package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import com.vuzeda.animewatchlist.tracker.domain.model.Anime

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState
    data class Success(
        val anime: Anime,
        val isNotificationsEnabled: Boolean = anime.isNotificationsEnabled,
        val isStatusSheetVisible: Boolean = false
    ) : DetailUiState
}
