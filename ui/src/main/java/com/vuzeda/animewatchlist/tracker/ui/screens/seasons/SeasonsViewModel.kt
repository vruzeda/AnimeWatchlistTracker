package com.vuzeda.animewatchlist.tracker.ui.screens.seasons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeSeason
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddSeasonsToAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FindAnimeBySeasonMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetSeasonAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ResolveAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateSeasonUseCase
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
    private val resolveAnimeUseCase: ResolveAnimeUseCase,
    private val addAnimeUseCase: AddAnimeUseCase,
    private val updateAnimeUseCase: UpdateAnimeUseCase,
    private val updateSeasonUseCase: UpdateSeasonUseCase,
    private val getSeasonsForAnimeUseCase: GetSeasonsForAnimeUseCase,
    private val addSeasonsToAnimeUseCase: AddSeasonsToAnimeUseCase,
    private val findAnimeBySeasonMalIdUseCase: FindAnimeBySeasonMalIdUseCase,
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
            combine(_rawAnimeList, _sortState, observeTitleLanguageUseCase()) { animeList, sortState, titleLanguage ->
                Triple(sortSeasonResults(animeList, sortState.option, sortState.isAscending), sortState, titleLanguage)
            }.collect { (displayedAnimeList, sortState, titleLanguage) ->
                _uiState.update {
                    it.copy(
                        displayedAnimeList = displayedAnimeList,
                        sortOption = sortState.option,
                        isSortAscending = sortState.isAscending,
                        titleLanguage = titleLanguage
                    )
                }
            }
        }

        loadSeason(year = currentYear, season = currentSeason)
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
                    _rawAnimeList.update { it + page.results }
                    _uiState.update {
                        it.copy(
                            animeList = it.animeList + page.results,
                            hasNextPage = page.hasNextPage,
                            currentPage = page.currentPage,
                            isLoadingMore = false
                        )
                    }
                    checkAddedResults(page.results)
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

    fun dismissBottomSheet() {
        pendingDetails = null
        _uiState.update { it.copy(selectedResultForAdd = null) }
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
                    checkAddedResults(page.results)
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
