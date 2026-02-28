package com.vuzeda.animewatchlist.tracker.ui.screens.search

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

enum class SearchSortOption(val displayLabel: String, val defaultAscending: Boolean) {
    ALPHABETICAL("Alphabetical", true),
    RECENTLY_ADDED("Recently Added", false),
    DEFAULT("Relevance", true),
    SCORE("Score", false)
}

enum class SearchFilter(val displayLabel: String) {
    ALL("All"),
    NOT_ADDED("Not Added"),
    ALREADY_ADDED("Already Added")
}

data class WatchlistEntry(
    val localId: Long,
    val status: WatchStatus,
    val addedAt: Long = 0
)

data class SearchUiState(
    val query: String = "",
    val results: List<Anime> = emptyList(),
    val displayedResults: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val selectedAnimeForAdd: Anime? = null,
    val watchlistEntries: Map<Int, WatchlistEntry> = emptyMap(),
    val sortOption: SearchSortOption = SearchSortOption.DEFAULT,
    val isSortAscending: Boolean = SearchSortOption.DEFAULT.defaultAscending,
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val snackbarMessage: String? = null,
    val pendingNavigationId: Long? = null,
    val pendingNavigationMalId: Int? = null
)
