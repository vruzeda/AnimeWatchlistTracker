package com.vuzeda.animewatchlist.tracker.module.ui.screens.search

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchOrderBy
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.SearchFilterState
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.ui.R

val AnimeSearchOrderBy.displayLabelRes: Int
    get() = when (this) {
        AnimeSearchOrderBy.DEFAULT -> R.string.search_order_default
        AnimeSearchOrderBy.SCORE -> R.string.search_order_score
        AnimeSearchOrderBy.RANK -> R.string.search_order_rank
        AnimeSearchOrderBy.POPULARITY -> R.string.search_order_popularity
        AnimeSearchOrderBy.MEMBERS -> R.string.search_order_members
        AnimeSearchOrderBy.FAVORITES -> R.string.search_order_favorites
        AnimeSearchOrderBy.START_DATE -> R.string.search_order_start_date
        AnimeSearchOrderBy.TITLE -> R.string.search_order_title
    }

val AnimeSearchType.displayLabelRes: Int
    get() = when (this) {
        AnimeSearchType.ALL -> R.string.anime_type_all
        AnimeSearchType.TV -> R.string.anime_type_tv
        AnimeSearchType.MOVIE -> R.string.anime_type_movie
        AnimeSearchType.OVA -> R.string.anime_type_ova
        AnimeSearchType.SPECIAL -> R.string.anime_type_special
        AnimeSearchType.ONA -> R.string.anime_type_ona
        AnimeSearchType.MUSIC -> R.string.anime_type_music
    }

val AnimeSearchStatus.displayLabelRes: Int
    get() = when (this) {
        AnimeSearchStatus.ALL -> R.string.anime_type_all
        AnimeSearchStatus.AIRING -> R.string.search_status_airing
        AnimeSearchStatus.COMPLETE -> R.string.search_status_complete
        AnimeSearchStatus.UPCOMING -> R.string.search_status_upcoming
    }

data class SearchDisplayData(
    val filterState: SearchFilterState,
    val titleLanguage: TitleLanguage,
    val addedMalIds: Set<Int>
)

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasNextPage: Boolean = false,
    val currentPage: Int = 1,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val selectedResultForAdd: SearchResult? = null,
    val selectedResultForDelete: SearchResult? = null,
    val filterState: SearchFilterState = SearchFilterState(),
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val snackbarMessage: String? = null,
    val pendingNavigationMalId: Int? = null,
    val addedMalIds: Set<Int> = emptySet(),
    val resolvingMalId: Int? = null,
    val isRefreshing: Boolean = false
)
