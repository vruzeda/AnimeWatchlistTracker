package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveWatchlistAnimeByMalIdsUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.SearchAnimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchAnimeUseCase: SearchAnimeUseCase,
    private val addAnimeToWatchlistUseCase: AddAnimeToWatchlistUseCase,
    private val observeWatchlistAnimeByMalIdsUseCase: ObserveWatchlistAnimeByMalIdsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var watchlistObservationJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            searchAnimeUseCase(query)
                .onSuccess { results ->
                    _uiState.update {
                        val displayed = computeDisplayedResults(
                            results = results,
                            watchlistEntries = it.watchlistEntries,
                            filter = it.selectedFilter,
                            sortOption = it.sortOption,
                            isAscending = it.isSortAscending
                        )
                        it.copy(
                            results = results,
                            displayedResults = displayed,
                            isLoading = false,
                            hasSearched = true
                        )
                    }
                    observeWatchlistForResults(results)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Search failed",
                            hasSearched = true
                        )
                    }
                }
        }
    }

    fun selectSort(option: SearchSortOption) {
        _uiState.update { currentState ->
            val isAscending = if (option == currentState.sortOption) {
                !currentState.isSortAscending
            } else {
                option.defaultAscending
            }
            val displayed = computeDisplayedResults(
                results = currentState.results,
                watchlistEntries = currentState.watchlistEntries,
                filter = currentState.selectedFilter,
                sortOption = option,
                isAscending = isAscending
            )
            currentState.copy(
                sortOption = option,
                isSortAscending = isAscending,
                displayedResults = displayed
            )
        }
    }

    fun selectFilter(filter: SearchFilter) {
        _uiState.update { currentState ->
            val displayed = computeDisplayedResults(
                results = currentState.results,
                watchlistEntries = currentState.watchlistEntries,
                filter = filter,
                sortOption = currentState.sortOption,
                isAscending = currentState.isSortAscending
            )
            currentState.copy(selectedFilter = filter, displayedResults = displayed)
        }
    }

    fun onAnimeClick(anime: Anime) {
        val malId = anime.malId ?: return
        val entry = _uiState.value.watchlistEntries[malId]
        if (entry != null) {
            _uiState.update { it.copy(pendingNavigationId = entry.localId) }
        } else {
            _uiState.update { it.copy(pendingNavigationMalId = malId) }
        }
    }

    fun onAddClick(anime: Anime) {
        _uiState.update {
            it.copy(selectedAnimeForAdd = anime)
        }
    }

    fun onStatusSelected(status: WatchStatus) {
        val anime = _uiState.value.selectedAnimeForAdd ?: return
        val animeWithStatus = anime.copy(status = status)

        _uiState.update { it.copy(selectedAnimeForAdd = null) }

        viewModelScope.launch {
            addAnimeToWatchlistUseCase(animeWithStatus)
            _uiState.update {
                it.copy(snackbarMessage = "${anime.title} added to watchlist")
            }
        }
    }

    fun dismissBottomSheet() {
        _uiState.update { it.copy(selectedAnimeForAdd = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onNavigated() {
        _uiState.update { it.copy(pendingNavigationId = null, pendingNavigationMalId = null) }
    }

    private fun observeWatchlistForResults(results: List<Anime>) {
        watchlistObservationJob?.cancel()
        val malIds = results.mapNotNull { it.malId }
        if (malIds.isEmpty()) {
            _uiState.update {
                val displayed = computeDisplayedResults(
                    results = it.results,
                    watchlistEntries = emptyMap(),
                    filter = it.selectedFilter,
                    sortOption = it.sortOption,
                    isAscending = it.isSortAscending
                )
                it.copy(watchlistEntries = emptyMap(), displayedResults = displayed)
            }
            return
        }
        watchlistObservationJob = viewModelScope.launch {
            observeWatchlistAnimeByMalIdsUseCase(malIds).collect { animeList ->
                val entries = animeList.mapNotNull { anime ->
                    anime.malId?.let { malId ->
                        malId to WatchlistEntry(
                            localId = anime.id,
                            status = anime.status,
                            addedAt = anime.addedAt
                        )
                    }
                }.toMap()
                _uiState.update {
                    val displayed = computeDisplayedResults(
                        results = it.results,
                        watchlistEntries = entries,
                        filter = it.selectedFilter,
                        sortOption = it.sortOption,
                        isAscending = it.isSortAscending
                    )
                    it.copy(watchlistEntries = entries, displayedResults = displayed)
                }
            }
        }
    }
}

fun computeDisplayedResults(
    results: List<Anime>,
    watchlistEntries: Map<Int, WatchlistEntry>,
    filter: SearchFilter,
    sortOption: SearchSortOption,
    isAscending: Boolean = sortOption.defaultAscending
): List<Anime> {
    val filtered = when (filter) {
        SearchFilter.ALL -> results
        SearchFilter.NOT_ADDED -> results.filter { anime ->
            anime.malId?.let { watchlistEntries[it] } == null
        }
        SearchFilter.ALREADY_ADDED -> results.filter { anime ->
            anime.malId?.let { watchlistEntries[it] } != null
        }
    }
    val sorted = when (sortOption) {
        SearchSortOption.DEFAULT -> filtered
        SearchSortOption.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
        SearchSortOption.SCORE -> filtered.sortedByDescending { it.score ?: 0.0 }
        SearchSortOption.RECENTLY_ADDED -> filtered.sortedByDescending { anime ->
            anime.malId?.let { watchlistEntries[it]?.addedAt } ?: -1L
        }
    }
    val shouldReverse = isAscending != sortOption.defaultAscending
    return if (shouldReverse && sortOption != SearchSortOption.DEFAULT) sorted.reversed() else sorted
}
