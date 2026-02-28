package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetWatchlistAnimeByMalIdsUseCase
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
    private val addAnimeToWatchlistUseCase: AddAnimeToWatchlistUseCase,
    private val getWatchlistAnimeByMalIdsUseCase: GetWatchlistAnimeByMalIdsUseCase
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
                    val malIds = results.mapNotNull { it.malId }
                    val entries = buildWatchlistEntries(malIds)
                    _uiState.update {
                        it.copy(
                            results = results,
                            isLoading = false,
                            hasSearched = true,
                            watchlistEntries = entries
                        )
                    }
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
            val malId = anime.malId
            _uiState.update {
                it.copy(
                    watchlistEntries = if (malId != null) {
                        it.watchlistEntries + (malId to WatchlistEntry(localId = localId, status = status))
                    } else {
                        it.watchlistEntries
                    },
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

    private suspend fun buildWatchlistEntries(malIds: List<Int>): Map<Int, WatchlistEntry> {
        if (malIds.isEmpty()) return emptyMap()
        return getWatchlistAnimeByMalIdsUseCase(malIds)
            .mapNotNull { anime ->
                anime.malId?.let { malId ->
                    malId to WatchlistEntry(localId = anime.id, status = anime.status)
                }
            }
            .toMap()
    }
}
