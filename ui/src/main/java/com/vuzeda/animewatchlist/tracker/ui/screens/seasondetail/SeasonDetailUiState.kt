package com.vuzeda.animewatchlist.tracker.ui.screens.seasondetail

import com.vuzeda.animewatchlist.tracker.domain.model.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.domain.model.Season

sealed interface SeasonDetailUiState {
    data object Loading : SeasonDetailUiState
    data object NotFound : SeasonDetailUiState
    data class Success(
        val season: Season,
        val isInWatchlist: Boolean = true,
        val episodes: List<EpisodeInfo> = emptyList(),
        val isLoadingEpisodes: Boolean = false,
        val hasMoreEpisodes: Boolean = false,
        val nextEpisodePage: Int = 1
    ) : SeasonDetailUiState
}
