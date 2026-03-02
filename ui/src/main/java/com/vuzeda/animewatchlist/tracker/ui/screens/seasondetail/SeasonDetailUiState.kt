package com.vuzeda.animewatchlist.tracker.ui.screens.seasondetail

import com.vuzeda.animewatchlist.tracker.domain.model.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage

sealed interface SeasonDetailUiState {
    data object Loading : SeasonDetailUiState
    data object NotFound : SeasonDetailUiState
    data class Success(
        val season: Season,
        val isInWatchlist: Boolean = true,
        val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
        val episodes: List<EpisodeInfo> = emptyList(),
        val isLoadingEpisodes: Boolean = false,
        val hasMoreEpisodes: Boolean = false,
        val nextEpisodePage: Int = 1,
        val isDeleteConfirmationVisible: Boolean = false,
        val isDeleted: Boolean = false,
        val isAddSheetVisible: Boolean = false,
        val isEpisodeNotificationsEnabled: Boolean = season.isEpisodeNotificationsEnabled,
        val snackbarMessage: String? = null,
        val pendingNavigationMalId: Int? = null
    ) : SeasonDetailUiState
}
