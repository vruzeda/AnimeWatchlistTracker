package com.vuzeda.animewatchlist.tracker.module.ui.screens.search

import androidx.annotation.StringRes
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SearchSortOption
import com.vuzeda.animewatchlist.tracker.module.domain.SearchSortState
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.ui.R

val SearchSortOption.displayLabelRes: Int
    get() = when (this) {
        SearchSortOption.DEFAULT -> R.string.sort_relevance
        SearchSortOption.ALPHABETICAL -> R.string.sort_alphabetical
        SearchSortOption.SCORE -> R.string.sort_score
    }

enum class SearchFilter(@StringRes val displayLabelRes: Int) {
    ALL(R.string.filter_all)
}

data class SearchDisplayData(
    val displayedResults: List<SearchResult>,
    val sortState: SearchSortState,
    val titleLanguage: TitleLanguage,
    val addedMalIds: Set<Int>
)

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val displayedResults: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val selectedResultForAdd: SearchResult? = null,
    val selectedResultForDelete: SearchResult? = null,
    val sortOption: SearchSortOption = SearchSortOption.DEFAULT,
    val isSortAscending: Boolean = SearchSortOption.DEFAULT.defaultAscending,
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val snackbarMessage: String? = null,
    val pendingNavigationMalId: Int? = null,
    val addedMalIds: Set<Int> = emptySet(),
    val resolvingMalId: Int? = null,
    val isRefreshing: Boolean = false
)
