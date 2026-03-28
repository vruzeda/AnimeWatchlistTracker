package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeFromDetailsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.GetSeasonAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveWatchlistMalIdsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RemoveAnimeByMalIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SeasonsViewModel @Inject constructor(
    private val getSeasonAnimeUseCase: GetSeasonAnimeUseCase,
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase,
    private val addAnimeFromDetailsUseCase: AddAnimeFromDetailsUseCase,
    private val removeAnimeByMalIdUseCase: RemoveAnimeByMalIdUseCase,
    private val observeWatchlistMalIdsUseCase: ObserveWatchlistMalIdsUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase
) : ViewModel() {

    private val _rawAnimeList = MutableStateFlow<List<SearchResult>>(emptyList())
    private val _sortState = MutableStateFlow(SeasonsSortState())
    private val _uiState = MutableStateFlow(SeasonsUiState())
    val uiState: StateFlow<SeasonsUiState> = _uiState.asStateFlow()

    private var pendingDetails: AnimeFullDetails? = null

    init {
        val now = LocalDate.now()
        val currentSeason = seasonFromMonth(now.monthValue)
        val currentYear = now.year

        _uiState.update {
            it.copy(
                selectedYear = currentYear,
                selectedSeason = currentSeason,
                currentYear = currentYear,
                currentSeason = currentSeason
            )
        }

        viewModelScope.launch {
            combine(
                _rawAnimeList,
                _sortState,
                observeTitleLanguageUseCase(),
                observeWatchlistMalIdsUseCase()
            ) { animeList, sortState, titleLanguage, watchlistMalIds ->
                SeasonsDisplayData(
                    displayedAnimeList = sortSeasonResults(animeList, sortState.option, sortState.isAscending),
                    sortState = sortState,
                    titleLanguage = titleLanguage,
                    addedMalIds = watchlistMalIds
                )
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        displayedAnimeList = data.displayedAnimeList,
                        sortOption = data.sortState.option,
                        isSortAscending = data.sortState.isAscending,
                        titleLanguage = data.titleLanguage,
                        addedMalIds = data.addedMalIds
                    )
                }
            }
        }

        loadSeason(year = currentYear, season = currentSeason)
    }

    fun refresh() {
        val state = _uiState.value
        _rawAnimeList.value = emptyList()
        _uiState.update {
            it.copy(animeList = emptyList(), hasNextPage = false, currentPage = 1, errorMessage = null, isRefreshing = true)
        }
        viewModelScope.launch {
            getSeasonAnimeUseCase(year = state.selectedYear, season = state.selectedSeason, page = 1)
                .onSuccess { page ->
                    _rawAnimeList.value = page.results
                    _uiState.update {
                        it.copy(
                            animeList = page.results,
                            hasNextPage = page.hasNextPage,
                            currentPage = page.currentPage,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isRefreshing = false, errorMessage = error.message) }
                }
        }
    }

    fun selectSort(option: SeasonsSortOption) {
        _sortState.update { current ->
            val isAscending = if (option == current.option) !current.isAscending else option.defaultAscending
            current.copy(option = option, isAscending = isAscending)
        }
    }

    fun selectNextSeason() {
        val state = _uiState.value
        val (nextSeason, yearDelta) = state.selectedSeason.next()
        val nextYear = state.selectedYear + yearDelta
        _rawAnimeList.value = emptyList()
        _uiState.update {
            it.copy(
                selectedYear = nextYear,
                selectedSeason = nextSeason,
                animeList = emptyList(),
                hasNextPage = false,
                currentPage = 1,
                errorMessage = null
            )
        }
        loadSeason(year = nextYear, season = nextSeason)
    }

    fun selectPreviousSeason() {
        val state = _uiState.value
        val (prevSeason, yearDelta) = state.selectedSeason.previous()
        val prevYear = state.selectedYear + yearDelta
        _rawAnimeList.value = emptyList()
        _uiState.update {
            it.copy(
                selectedYear = prevYear,
                selectedSeason = prevSeason,
                animeList = emptyList(),
                hasNextPage = false,
                currentPage = 1,
                errorMessage = null
            )
        }
        loadSeason(year = prevYear, season = prevSeason)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasNextPage) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = state.currentPage + 1
            getSeasonAnimeUseCase(
                year = state.selectedYear,
                season = state.selectedSeason,
                page = nextPage
            )
                .onSuccess { page ->
                    _rawAnimeList.update { existing ->
                        val seenIds = existing.mapTo(mutableSetOf()) { it.malId }
                        existing + page.results.filter { it.malId !in seenIds }
                    }
                    _uiState.update {
                        val seenIds = it.animeList.mapTo(mutableSetOf()) { item -> item.malId }
                        it.copy(
                            animeList = it.animeList + page.results.filter { item -> item.malId !in seenIds },
                            hasNextPage = page.hasNextPage,
                            currentPage = page.currentPage,
                            isLoadingMore = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    fun onResultClick(result: SearchResult) {
        _uiState.update { it.copy(pendingNavigationMalId = result.malId) }
    }

    fun onNavigated() {
        _uiState.update { it.copy(pendingNavigationMalId = null) }
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
                        it.copy(resolvingMalId = null)
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

    private fun loadSeason(year: Int, season: AnimeSeason) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getSeasonAnimeUseCase(year = year, season = season, page = 1)
                .onSuccess { page ->
                    _rawAnimeList.value = page.results
                    _uiState.update {
                        it.copy(
                            animeList = page.results,
                            hasNextPage = page.hasNextPage,
                            currentPage = page.currentPage,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    companion object {
        fun seasonFromMonth(month: Int): AnimeSeason = when (month) {
            in 1..3 -> AnimeSeason.WINTER
            in 4..6 -> AnimeSeason.SPRING
            in 7..9 -> AnimeSeason.SUMMER
            else -> AnimeSeason.FALL
        }
    }
}

fun sortSeasonResults(
    results: List<SearchResult>,
    sortOption: SeasonsSortOption,
    isAscending: Boolean = sortOption.defaultAscending
): List<SearchResult> {
    val sorted = when (sortOption) {
        SeasonsSortOption.DEFAULT -> results
        SeasonsSortOption.ALPHABETICAL -> results.sortedBy { it.title.lowercase() }
        SeasonsSortOption.SCORE -> results.sortedByDescending { it.score ?: 0.0 }
    }
    val shouldReverse = isAscending != sortOption.defaultAscending
    return if (shouldReverse && sortOption != SeasonsSortOption.DEFAULT) sorted.reversed() else sorted
}
