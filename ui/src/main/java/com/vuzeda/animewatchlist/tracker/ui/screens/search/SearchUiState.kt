package com.vuzeda.animewatchlist.tracker.ui.screens.search

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

data class WatchlistEntry(
    val localId: Long,
    val status: WatchStatus
)

data class SearchUiState(
    val query: String = "",
    val results: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val selectedAnimeForAdd: Anime? = null,
    val isNavigateAfterAdd: Boolean = false,
    val watchlistEntries: Map<Int, WatchlistEntry> = emptyMap(),
    val snackbarMessage: String? = null,
    val pendingNavigationId: Long? = null
)
