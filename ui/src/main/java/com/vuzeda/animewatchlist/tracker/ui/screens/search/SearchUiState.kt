package com.vuzeda.animewatchlist.tracker.ui.screens.search

import com.vuzeda.animewatchlist.tracker.domain.model.Anime

data class SearchUiState(
    val query: String = "",
    val results: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val selectedAnimeForAdd: Anime? = null,
    val isNavigateAfterAdd: Boolean = false,
    val addedAnimeIds: Map<Int, Long> = emptyMap(),
    val snackbarMessage: String? = null,
    val pendingNavigationId: Long? = null
)
