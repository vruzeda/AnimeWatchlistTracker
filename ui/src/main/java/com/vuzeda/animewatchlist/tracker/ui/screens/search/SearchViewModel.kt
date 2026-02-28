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
                            sortOption = it.sortOption
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
            val displayed = computeDisplayedResults(
                results = currentState.results,
                watchlistEntries = currentState.watchlistEntries,
                filter = currentState.selectedFilter,
                sortOption = option
            )
            currentState.copy(sortOption = option, displayedResults = displayed)
        }
    }

    fun selectFilter(filter: SearchFilter) {
        _uiState.update { currentState ->
            val displayed = computeDisplayedResults(
                results = currentState.results,
                watchlistEntries = currentState.watchlistEntries,
                filter = filter,
                sortOption = currentState.sortOption
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
            _uiState.update {
                it.copy(selectedAnimeForAdd = anime, isNavigateAfterAdd = true)
            }
        }
    }

    fun onAddClick(anime: Anime) {
        _uiState.update {
            it.copy(selectedAnimeForAdd = anime, isNavigateAfterAdd = false)
        }
    }

    fun onStatusSelected(status: WatchStatus) {
        val anime = _uiState.value.selectedAnimeForAdd ?: return
        val shouldNavigate = _uiState.value.isNavigateAfterAdd
        val animeWithStatus = anime.copy(status = status)

        _uiState.update {
            it.copy(selectedAnimeForAdd = null, isNavigateAfterAdd = false)
        }

        viewModelScope.launch {
            val localId = addAnimeToWatchlistUseCase(animeWithStatus)
            _uiState.update {
                it.copy(
                    snackbarMessage = "${anime.title} added to watchlist",
                    pendingNavigationId = if (shouldNavigate) localId else null
                )
            }
        }
    }

    fun dismissBottomSheet() {
        _uiState.update {
            it.copy(selectedAnimeForAdd = null, isNavigateAfterAdd = false)
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onNavigated() {
        _uiState.update { it.copy(pendingNavigationId = null) }
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
                    sortOption = it.sortOption
                )
                it.copy(watchlistEntries = emptyMap(), displayedResults = displayed)
            }
            return
        }
        watchlistObservationJob = viewModelScope.launch {
            observeWatchlistAnimeByMalIdsUseCase(malIds).collect { animeList ->
                val entries = animeList.mapNotNull { anime ->
                    anime.malId?.let { malId ->
                        malId to WatchlistEntry(localId = anime.id, status = anime.status)
                    }
                }.toMap()
                _uiState.update {
                    val displayed = computeDisplayedResults(
                        results = it.results,
                        watchlistEntries = entries,
                        filter = it.selectedFilter,
                        sortOption = it.sortOption
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
    sortOption: SearchSortOption
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
    return when (sortOption) {
        SearchSortOption.DEFAULT -> filtered
        SearchSortOption.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
        SearchSortOption.SCORE -> filtered.sortedByDescending { it.score ?: 0.0 }
    }
}
