package com.vuzeda.animewatchlist.tracker.ui.screens.animedetail

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage

sealed interface AnimeDetailUiState {
    data object Loading : AnimeDetailUiState
    data object NotFound : AnimeDetailUiState
    data class Success(
        val anime: Anime,
        val seasons: List<Season> = emptyList(),
        val isInWatchlist: Boolean = true,
        val isNotificationsEnabled: Boolean = anime.isNotificationsEnabled,
        val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
        val isResolvingPrequels: Boolean = false,
        val isResolvingSequels: Boolean = false,
        val isStatusSheetVisible: Boolean = false,
        val isAddSheetVisible: Boolean = false,
        val snackbarMessage: String? = null
    ) : AnimeDetailUiState
}
