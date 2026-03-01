package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.annotation.StringRes
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.R

enum class SearchSortOption(@param:StringRes val displayLabelRes: Int, val defaultAscending: Boolean) {
    ALPHABETICAL(R.string.sort_alphabetical, true),
    DEFAULT(R.string.sort_relevance, true),
    SCORE(R.string.sort_score, false)
}

data class SearchSortState(
    val option: SearchSortOption = SearchSortOption.DEFAULT,
    val isAscending: Boolean = SearchSortOption.DEFAULT.defaultAscending
)

enum class SearchFilter(@param:StringRes val displayLabelRes: Int) {
    ALL(R.string.filter_all)
}

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val displayedResults: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val selectedResultForAdd: SearchResult? = null,
    val sortOption: SearchSortOption = SearchSortOption.DEFAULT,
    val isSortAscending: Boolean = SearchSortOption.DEFAULT.defaultAscending,
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val snackbarMessage: String? = null,
    val pendingNavigationMalId: Int? = null,
    val addedMalIds: Set<Int> = emptySet(),
    val resolvingMalId: Int? = null
)
