package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddSeasonsToAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FindAnimeBySeasonMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ResolveAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.SearchAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateSeasonUseCase
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
    private val resolveAnimeUseCase: ResolveAnimeUseCase,
    private val addAnimeUseCase: AddAnimeUseCase,
    private val updateAnimeUseCase: UpdateAnimeUseCase,
    private val updateSeasonUseCase: UpdateSeasonUseCase,
    private val getSeasonsForAnimeUseCase: GetSeasonsForAnimeUseCase,
    private val addSeasonsToAnimeUseCase: AddSeasonsToAnimeUseCase,
    private val findAnimeBySeasonMalIdUseCase: FindAnimeBySeasonMalIdUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase
) : ViewModel() {

    private val _rawResults = MutableStateFlow<List<SearchResult>>(emptyList())
    private val _sortState = MutableStateFlow(SearchSortState())
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var pendingDetails: AnimeFullDetails? = null

    init {
        viewModelScope.launch {
            combine(_rawResults, _sortState, observeTitleLanguageUseCase()) { results, sortState, titleLanguage ->
                Triple(sortResults(results, sortState.option, sortState.isAscending), sortState, titleLanguage)
            }.collect { (displayedResults, sortState, titleLanguage) ->
                _uiState.update {
                    it.copy(
                        displayedResults = displayedResults,
                        sortOption = sortState.option,
                        isSortAscending = sortState.isAscending,
                        titleLanguage = titleLanguage
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
            val anime = Anime(
                title = details.title,
                titleEnglish = details.titleEnglish,
                titleJapanese = details.titleJapanese,
                imageUrl = details.imageUrl,
                synopsis = details.synopsis,
                genres = details.genres
            )
            val season = Season(
                malId = details.malId,
                title = details.title,
                titleEnglish = details.titleEnglish,
                titleJapanese = details.titleJapanese,
                imageUrl = details.imageUrl,
                type = details.type,
                episodeCount = details.episodes,
                score = details.score,
                airingStatus = details.airingStatus,
                orderIndex = 0
            )

            val animeId = addAnimeUseCase(
                anime = anime,
                seasons = listOf(season),
                status = status
            )

            pendingDetails = null
            _uiState.update {
                it.copy(
                    selectedResultForAdd = null,
                    addedMalIds = it.addedMalIds + details.malId,
                    snackbarMessage = result?.title
                )
            }

            resolveRemainingSeasonsInBackground(animeId, details, status)
        }
    }

    private suspend fun resolveRemainingSeasonsInBackground(
        animeId: Long,
        initialDetails: AnimeFullDetails,
        status: WatchStatus
    ) {
        resolveAnimeUseCase(initialDetails.malId)
            .onSuccess { resolved ->
                val existingSeasons = getSeasonsForAnimeUseCase(animeId)
                val existingMalIds = existingSeasons.map { it.malId }.toSet()

                val resolvedSeasonEntries = resolved.seasons.mapIndexed { index, seasonData ->
                    seasonData to index
                }

                val initialEntry = existingSeasons.firstOrNull { it.malId == initialDetails.malId }
                val correctOrderIndex = resolvedSeasonEntries
                    .firstOrNull { it.first.malId == initialDetails.malId }
                    ?.second ?: 0

                if (initialEntry != null && initialEntry.orderIndex != correctOrderIndex) {
                    updateSeasonUseCase(initialEntry.copy(orderIndex = correctOrderIndex))
                }

                val remainingSeasons = resolvedSeasonEntries
                    .filter { it.first.malId !in existingMalIds }
                    .map { (seasonData, index) ->
                        Season(
                            malId = seasonData.malId,
                            title = seasonData.title,
                            titleEnglish = seasonData.titleEnglish,
                            titleJapanese = seasonData.titleJapanese,
                            imageUrl = seasonData.imageUrl,
                            type = seasonData.type,
                            episodeCount = seasonData.episodeCount,
                            score = seasonData.score,
                            airingStatus = seasonData.airingStatus,
                            orderIndex = index
                        )
                    }

                if (remainingSeasons.isNotEmpty()) {
                    addSeasonsToAnimeUseCase(animeId, remainingSeasons)
                }

                updateAnimeUseCase(
                    Anime(
                        id = animeId,
                        title = resolved.title,
                        titleEnglish = resolved.titleEnglish,
                        titleJapanese = resolved.titleJapanese,
                        imageUrl = resolved.imageUrl,
                        synopsis = resolved.synopsis,
                        genres = resolved.genres,
                        status = status,
                        addedAt = System.currentTimeMillis()
                    )
                )

                val allResolvedMalIds = resolved.seasons.map { it.malId }.toSet()
                _uiState.update { it.copy(addedMalIds = it.addedMalIds + allResolvedMalIds) }
            }
    }

    fun dismissBottomSheet() {
        pendingDetails = null
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
