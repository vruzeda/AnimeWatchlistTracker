package com.vuzeda.animewatchlist.tracker.ui.screens.search

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

enum class SearchSortOption(val displayLabel: String) {
    DEFAULT("Relevance"),
    ALPHABETICAL("Alphabetical"),
    SCORE("Score")
}

enum class SearchFilter(val displayLabel: String) {
    ALL("All"),
    NOT_ADDED("Not Added"),
    ALREADY_ADDED("Already Added")
}

data class WatchlistEntry(
    val localId: Long,
    val status: WatchStatus
)

data class SearchUiState(
    val query: String = "",
    val results: List<Anime> = emptyList(),
    val displayedResults: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val selectedAnimeForAdd: Anime? = null,
    val isNavigateAfterAdd: Boolean = false,
    val watchlistEntries: Map<Int, WatchlistEntry> = emptyMap(),
    val sortOption: SearchSortOption = SearchSortOption.DEFAULT,
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val snackbarMessage: String? = null,
    val pendingNavigationId: Long? = null
)
