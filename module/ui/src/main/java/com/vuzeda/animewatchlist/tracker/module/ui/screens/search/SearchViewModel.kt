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
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSearchFilterStateUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveWatchlistMalIdsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RemoveAnimeByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SearchAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetSearchFilterStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var pendingDetails: AnimeFullDetails? = null

    init {
        viewModelScope.launch {
            combine(
                observeSearchFilterStateUseCase(),
                observeTitleLanguageUseCase(),
                observeWatchlistMalIdsUseCase()
            ) { filterState, titleLanguage, watchlistMalIds ->
                SearchDisplayData(
                    filterState = filterState,
                    titleLanguage = titleLanguage,
                    addedMalIds = watchlistMalIds
                )
            }.collect { data ->
                val previousFilter = _uiState.value.filterState
                val hasSearched = _uiState.value.hasSearched
                val query = _uiState.value.query.trim()
                _uiState.update {
                    it.copy(
                        filterState = data.filterState,
                        titleLanguage = data.titleLanguage,
                        addedMalIds = data.addedMalIds
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

    private fun performSearch(query: String) {
        val filterState = _uiState.value.filterState
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            searchAnimeUseCase(query, filterState)
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            results = results,
                            isLoading = false,
                            hasSearched = true
                        )
                    }
                    analyticsTracker.track(
                        AnalyticsEvent.ExecuteSearch(query.length, results.size, true)
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                            hasSearched = true
                        )
                    }
                    analyticsTracker.track(
                        AnalyticsEvent.ExecuteSearch(query.length, 0, false)
                    )
                }
        }
    }

    fun refresh() {
        if (!_uiState.value.hasSearched) return
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        val filterState = _uiState.value.filterState

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            searchAnimeUseCase(query, filterState)
                .onSuccess { results ->
                    _uiState.update { it.copy(results = results, isRefreshing = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isRefreshing = false, errorMessage = error.message) }
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
                        it.copy(
                            resolvingMalId = null,
                            snackbarMessage = it.snackbarMessage
                        )
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

    fun onNavigated() {
        _uiState.update { it.copy(pendingNavigationMalId = null) }
    }
}
