package com.vuzeda.animewatchlist.tracker.module.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsEvent
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchOrderBy
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeFromDetailsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveIsSearchFilteringAvailableUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSearchFilterStateUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveWatchlistMalIdsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RemoveAnimeByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SearchAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetSearchFilterStateUseCase
import com.vuzeda.animewatchlist.tracker.module.ui.screens.toLoadErrorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchAnimeUseCase: SearchAnimeUseCase,
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase,
    private val addAnimeFromDetailsUseCase: AddAnimeFromDetailsUseCase,
    private val removeAnimeByMalIdUseCase: RemoveAnimeByMalIdUseCase,
    private val observeWatchlistMalIdsUseCase: ObserveWatchlistMalIdsUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val observeSearchFilterStateUseCase: ObserveSearchFilterStateUseCase,
    private val setSearchFilterStateUseCase: SetSearchFilterStateUseCase,
    private val observeIsSearchFilteringAvailableUseCase: ObserveIsSearchFilteringAvailableUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var pendingDetails: AnimeFullDetails? = null
    private var searchJob: Job? = null
    private var currentQueryGeneration = 0

    init {
        viewModelScope.launch {
            combine(
                observeSearchFilterStateUseCase(),
                observeTitleLanguageUseCase(),
                observeWatchlistMalIdsUseCase(),
                observeIsSearchFilteringAvailableUseCase()
            ) { filterState, titleLanguage, watchlistMalIds, areFiltersAvailable ->
                SearchDisplayData(
                    filterState = filterState,
                    titleLanguage = titleLanguage,
                    addedMalIds = watchlistMalIds,
                    areFiltersAvailable = areFiltersAvailable
                )
            }.collect { data ->
                val previousFilter = _uiState.value.filterState
                val hasSearched = _uiState.value.hasSearched
                val query = _uiState.value.query.trim()
                _uiState.update {
                    it.copy(
                        filterState = data.filterState,
                        titleLanguage = data.titleLanguage,
                        addedMalIds = data.addedMalIds,
                        areFiltersAvailable = data.areFiltersAvailable
                    )
                }
                if (data.filterState != previousFilter && hasSearched && query.isNotBlank()) {
                    performSearch(query)
                }
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        performSearch(query)
    }

    fun retry() {
        if (!_uiState.value.hasSearched) return
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        performSearch(query)
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        val filterState = _uiState.value.filterState
        currentQueryGeneration++
        val generation = currentQueryGeneration

        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadError = null,
                    results = emptyList(),
                    hasNextPage = false,
                    currentPage = 1
                )
            }
            searchAnimeUseCase(query, filterState, page = 1)
                .onSuccess { page ->
                    if (generation == currentQueryGeneration) {
                        _uiState.update {
                            it.copy(
                                results = page.results,
                                hasNextPage = page.hasNextPage,
                                currentPage = page.currentPage,
                                isLoading = false,
                                hasSearched = true
                            )
                        }
                        analyticsTracker.track(
                            AnalyticsEvent.ExecuteSearch(query.length, page.results.size, true)
                        )
                    }
                }
                .onFailure { error ->
                    if (generation == currentQueryGeneration) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadError = error.toLoadErrorType(),
                                hasSearched = true
                            )
                        }
                        analyticsTracker.track(
                            AnalyticsEvent.ExecuteSearch(query.length, 0, false)
                        )
                    }
                }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasNextPage) return
        val query = state.query.trim()
        if (query.isBlank()) return
        val nextPage = state.currentPage + 1
        val generation = currentQueryGeneration

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            analyticsTracker.track(AnalyticsEvent.LoadMoreResults("search", nextPage))
            searchAnimeUseCase(query, state.filterState, page = nextPage)
                .onSuccess { page ->
                    if (generation == currentQueryGeneration) {
                        _uiState.update {
                            it.copy(
                                results = (it.results + page.results).distinctBy { item -> item.malId },
                                hasNextPage = page.hasNextPage,
                                currentPage = page.currentPage,
                                isLoadingMore = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoadingMore = false) }
                    }
                }
                .onFailure {
                    if (generation == currentQueryGeneration) {
                        _uiState.update { it.copy(isLoadingMore = false, snackbarEvent = SearchSnackbarEvent.LoadMoreFailed) }
                    } else {
                        _uiState.update { it.copy(isLoadingMore = false) }
                    }
                }
        }
    }

    fun refresh() {
        if (!_uiState.value.hasSearched) return
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        val filterState = _uiState.value.filterState
        val generation = currentQueryGeneration

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            searchAnimeUseCase(query, filterState, page = 1)
                .onSuccess { page ->
                    if (generation == currentQueryGeneration) {
                        _uiState.update {
                            it.copy(
                                results = page.results,
                                hasNextPage = page.hasNextPage,
                                currentPage = page.currentPage,
                                isRefreshing = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isRefreshing = false) }
                    }
                }
                .onFailure {
                    if (generation == currentQueryGeneration) {
                        _uiState.update { it.copy(isRefreshing = false, snackbarEvent = SearchSnackbarEvent.RefreshFailed) }
                    } else {
                        _uiState.update { it.copy(isRefreshing = false) }
                    }
                }
        }
    }

    fun selectSort(orderBy: AnimeSearchOrderBy) {
        val current = _uiState.value.filterState
        val isAscending = if (orderBy == current.orderBy) !current.isAscending else orderBy.defaultAscending
        viewModelScope.launch {
            setSearchFilterStateUseCase(current.copy(orderBy = orderBy, isAscending = isAscending))
        }
        analyticsTracker.track(AnalyticsEvent.SelectSort("search", orderBy.name, isAscending))
    }

    fun selectType(type: AnimeSearchType) {
        viewModelScope.launch {
            setSearchFilterStateUseCase(_uiState.value.filterState.copy(type = type))
        }
        analyticsTracker.track(AnalyticsEvent.SelectFilter("search_type", type.name))
    }

    fun selectStatus(status: AnimeSearchStatus) {
        viewModelScope.launch {
            setSearchFilterStateUseCase(_uiState.value.filterState.copy(status = status))
        }
        analyticsTracker.track(AnalyticsEvent.SelectFilter("search_status", status.name))
    }

    fun onResultClick(result: SearchResult) {
        _uiState.update { it.copy(pendingNavigationMalId = result.malId) }
    }

    fun onAddClick(result: SearchResult) {
        viewModelScope.launch {
            _uiState.update { it.copy(resolvingMalId = result.malId) }

            fetchSeasonDetailUseCase(result.malId)
                .onSuccess { details ->
                    pendingDetails = details
                    _uiState.update {
                        it.copy(
                            resolvingMalId = null,
                            selectedResultForAdd = result
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(resolvingMalId = null, snackbarEvent = SearchSnackbarEvent.DetailFetchFailed)
                    }
                }
        }
    }

    fun addToWatchlist(status: WatchStatus) {
        val details = pendingDetails ?: return
        val result = _uiState.value.selectedResultForAdd

        viewModelScope.launch {
            addAnimeFromDetailsUseCase(details, status)
            analyticsTracker.track(AnalyticsEvent.AddAnime(status.name, 1, false))

            pendingDetails = null
            _uiState.update {
                it.copy(
                    selectedResultForAdd = null,
                    snackbarMessage = result?.title
                )
            }
        }
    }

    fun dismissBottomSheet() {
        pendingDetails = null
        _uiState.update { it.copy(selectedResultForAdd = null) }
    }

    fun onRemoveClick(result: SearchResult) {
        _uiState.update { it.copy(selectedResultForDelete = result) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(selectedResultForDelete = null) }
    }

    fun confirmRemoveFromWatchlist() {
        val result = _uiState.value.selectedResultForDelete ?: return

        viewModelScope.launch {
            removeAnimeByMalIdUseCase(result.malId)
            analyticsTracker.track(AnalyticsEvent.RemoveAnime("UNKNOWN"))
            _uiState.update { it.copy(selectedResultForDelete = null) }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun clearSnackbarEvent() {
        _uiState.update { it.copy(snackbarEvent = null) }
    }

    fun onNavigated() {
        _uiState.update { it.copy(pendingNavigationMalId = null) }
    }
}
