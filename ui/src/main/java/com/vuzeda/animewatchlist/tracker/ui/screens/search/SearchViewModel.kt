package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.usecase.SearchAnimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchAnimeUseCase: SearchAnimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

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
                    val displayed = sortResults(results, _uiState.value.sortOption, _uiState.value.isSortAscending)
                    _uiState.update {
                        it.copy(
                            results = results,
                            displayedResults = displayed,
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
        _uiState.update { currentState ->
            val isAscending = if (option == currentState.sortOption) {
                !currentState.isSortAscending
            } else {
                option.defaultAscending
            }
            val displayed = sortResults(currentState.results, option, isAscending)
            currentState.copy(
                sortOption = option,
                isSortAscending = isAscending,
                displayedResults = displayed
            )
        }
    }

    fun onResultClick(result: SearchResult) {
        _uiState.update { it.copy(pendingNavigationMalId = result.malId) }
    }

    fun onAddClick(result: SearchResult) {
        _uiState.update { it.copy(selectedResultForAdd = result) }
    }

    fun dismissBottomSheet() {
        _uiState.update { it.copy(selectedResultForAdd = null) }
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
