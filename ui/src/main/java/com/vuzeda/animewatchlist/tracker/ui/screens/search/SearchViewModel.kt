package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FindAnimeBySeasonMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ResolveAnimeUseCase
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
    private val searchAnimeUseCase: SearchAnimeUseCase,
    private val resolveAnimeUseCase: ResolveAnimeUseCase,
    private val addAnimeUseCase: AddAnimeUseCase,
    private val findAnimeBySeasonMalIdUseCase: FindAnimeBySeasonMalIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var pendingResolvedAnime: Anime? = null
    private var pendingResolvedSeasons: List<Season> = emptyList()

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
                    checkAddedResults(results)
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
        viewModelScope.launch {
            _uiState.update { it.copy(resolvingMalId = result.malId) }

            resolveAnimeUseCase(result.malId)
                .onSuccess { resolved ->
                    pendingResolvedAnime = Anime(
                        title = resolved.title,
                        imageUrl = resolved.imageUrl,
                        synopsis = resolved.synopsis,
                        genres = resolved.genres
                    )
                    pendingResolvedSeasons = resolved.seasons.mapIndexed { index, seasonData ->
                        Season(
                            malId = seasonData.malId,
                            title = seasonData.title,
                            imageUrl = seasonData.imageUrl,
                            type = seasonData.type,
                            episodeCount = seasonData.episodeCount,
                            score = seasonData.score,
                            airingStatus = seasonData.airingStatus,
                            orderIndex = index
                        )
                    }
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
        val anime = pendingResolvedAnime ?: return
        val seasons = pendingResolvedSeasons
        val result = _uiState.value.selectedResultForAdd

        viewModelScope.launch {
            addAnimeUseCase(anime = anime, seasons = seasons, status = status)
            pendingResolvedAnime = null
            pendingResolvedSeasons = emptyList()

            val addedMalIds = _uiState.value.addedMalIds.toMutableSet()
            seasons.forEach { addedMalIds.add(it.malId) }

            _uiState.update {
                it.copy(
                    selectedResultForAdd = null,
                    addedMalIds = addedMalIds,
                    snackbarMessage = result?.title
                )
            }
        }
    }

    fun dismissBottomSheet() {
        pendingResolvedAnime = null
        pendingResolvedSeasons = emptyList()
        _uiState.update { it.copy(selectedResultForAdd = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onNavigated() {
        _uiState.update { it.copy(pendingNavigationMalId = null) }
    }

    private fun checkAddedResults(results: List<SearchResult>) {
        viewModelScope.launch {
            val addedMalIds = mutableSetOf<Int>()
            results.forEach { result ->
                val animeId = findAnimeBySeasonMalIdUseCase(result.malId)
                if (animeId != null) {
                    addedMalIds.add(result.malId)
                }
            }
            _uiState.update { it.copy(addedMalIds = it.addedMalIds + addedMalIds) }
        }
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
