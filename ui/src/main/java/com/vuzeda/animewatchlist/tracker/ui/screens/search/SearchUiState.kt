package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.annotation.StringRes
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.R

enum class SearchSortOption(@StringRes val displayLabelRes: Int, val defaultAscending: Boolean) {
    ALPHABETICAL(R.string.sort_alphabetical, true),
    RECENTLY_ADDED(R.string.sort_recently_added, false),
    DEFAULT(R.string.sort_relevance, true),
    SCORE(R.string.sort_score, false)
}

enum class SearchFilter(@StringRes val displayLabelRes: Int) {
    ALL(R.string.filter_all),
    NOT_ADDED(R.string.filter_not_added),
    ALREADY_ADDED(R.string.filter_already_added)
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
