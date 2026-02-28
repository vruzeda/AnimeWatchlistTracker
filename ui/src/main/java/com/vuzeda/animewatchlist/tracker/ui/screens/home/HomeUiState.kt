package com.vuzeda.animewatchlist.tracker.ui.screens.home

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

data class HomeUiState(
    val animeList: List<Anime> = emptyList(),
    val selectedTab: WatchStatus? = null,
    val isLoading: Boolean = true
)
