package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage

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
        val isLastSeason: Boolean = false,
        val isDeleteConfirmationVisible: Boolean = false,
        val isDeleted: Boolean = false,
        val isAddSheetVisible: Boolean = false,
        val isEpisodeNotificationsEnabled: Boolean = season.isEpisodeNotificationsEnabled,
        val snackbarEvent: SeasonDetailSnackbarEvent? = null,
        val pendingNavigationMalId: Int? = null
    ) : SeasonDetailUiState
}

sealed interface SeasonDetailSnackbarEvent {
    data class AddedToWatchlist(val title: String) : SeasonDetailSnackbarEvent
    data class EpisodeNotificationsToggled(val enabled: Boolean) : SeasonDetailSnackbarEvent
}
