package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.domain.model.RelatedAnime

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState
    data class Success(
        val anime: Anime,
        val isInWatchlist: Boolean = true,
        val isNotificationsEnabled: Boolean = anime.isNotificationsEnabled,
        val isStatusSheetVisible: Boolean = false,
        val isAddSheetVisible: Boolean = false,
        val episodes: List<EpisodeInfo> = emptyList(),
        val isLoadingEpisodes: Boolean = false,
        val hasMoreEpisodes: Boolean = false,
        val relatedAnime: List<RelatedAnime> = emptyList(),
        val isLoadingRelated: Boolean = false
    ) : DetailUiState
}
