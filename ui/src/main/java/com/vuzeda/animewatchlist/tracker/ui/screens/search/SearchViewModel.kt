package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeFromDetailsUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveWatchlistMalIdsUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.RemoveAnimeByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.SearchAnimeUseCase
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
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase
) : ViewModel() {

    private val _rawResults = MutableStateFlow<List<SearchResult>>(emptyList())
    private val _sortState = MutableStateFlow(SearchSortState())
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var pendingDetails: AnimeFullDetails? = null

    init {
        viewModelScope.launch {
            combine(
                _rawResults,
                _sortState,
                observeTitleLanguageUseCase(),
                observeWatchlistMalIdsUseCase()
            ) { results, sortState, titleLanguage, watchlistMalIds ->
                SearchDisplayData(
                    displayedResults = sortResults(results, sortState.option, sortState.isAscending),
                    sortState = sortState,
                    titleLanguage = titleLanguage,
                    addedMalIds = watchlistMalIds
                )
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        displayedResults = data.displayedResults,
                        sortOption = data.sortState.option,
                        isSortAscending = data.sortState.isAscending,
                        titleLanguage = data.titleLanguage,
                        addedMalIds = data.addedMalIds
                    )
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            searchAnimeUseCase(query)
                .onSuccess { results ->
                    _rawResults.value = results
                    _uiState.update {
                        it.copy(
                            results = results,
                            isLoading = false,
                            hasSearched = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                            hasSearched = true
                        )
                    }
                }
        }
    }

    fun selectSort(option: SearchSortOption) {
        _sortState.update { current ->
            val isAscending = if (option == current.option) !current.isAscending else option.defaultAscending
            current.copy(option = option, isAscending = isAscending)
        }
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

fun sortResults(
    results: List<SearchResult>,
    sortOption: SearchSortOption,
    isAscending: Boolean = sortOption.defaultAscending
): List<SearchResult> {
    val sorted = when (sortOption) {
        SearchSortOption.DEFAULT -> results
        SearchSortOption.ALPHABETICAL -> results.sortedBy { it.title.lowercase() }
        SearchSortOption.SCORE -> results.sortedByDescending { it.score ?: 0.0 }
    }
    val shouldReverse = isAscending != sortOption.defaultAscending
    return if (shouldReverse && sortOption != SearchSortOption.DEFAULT) sorted.reversed() else sorted
}
