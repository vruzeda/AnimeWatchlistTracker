package com.vuzeda.animewatchlist.tracker.ui.screens.seasondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveSeasonByIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateSeasonProgressUseCase
import com.vuzeda.animewatchlist.tracker.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeSeasonByIdUseCase: ObserveSeasonByIdUseCase,
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase,
    private val fetchEpisodesUseCase: FetchEpisodesUseCase,
    private val updateSeasonProgressUseCase: UpdateSeasonProgressUseCase
) : ViewModel() {

    private val seasonId: Long = checkNotNull(savedStateHandle[Route.SeasonDetail.ARG_SEASON_ID])
    private val malId: Int = savedStateHandle[Route.SeasonDetail.ARG_MAL_ID] ?: 0

    private val _uiState = MutableStateFlow<SeasonDetailUiState>(SeasonDetailUiState.Loading)
    val uiState: StateFlow<SeasonDetailUiState> = _uiState.asStateFlow()

    init {
        if (seasonId > 0) {
            observeSeason()
        } else if (malId > 0) {
            loadFromApi()
        } else {
            _uiState.value = SeasonDetailUiState.NotFound
        }
    }

    private fun observeSeason() {
        viewModelScope.launch {
            observeSeasonByIdUseCase(seasonId).collect { season ->
                if (season != null) {
                    _uiState.update { currentState ->
                        when (currentState) {
                            is SeasonDetailUiState.Success -> currentState.copy(season = season)
                            else -> {
                                loadEpisodes(season.malId, page = 1)
                                SeasonDetailUiState.Success(
                                    season = season,
                                    isLoadingEpisodes = true
                                )
                            }
                        }
                    }
                } else {
                    _uiState.value = SeasonDetailUiState.NotFound
                }
            }
        }
    }

    private fun loadFromApi() {
        viewModelScope.launch {
            fetchSeasonDetailUseCase(malId)
                .onSuccess { details ->
                    val season = Season(
                        malId = details.malId,
                        title = details.title,
                        imageUrl = details.imageUrl,
                        type = details.type,
                        episodeCount = details.episodes,
                        score = details.score,
                        airingStatus = details.airingStatus
                    )
                    loadEpisodes(details.malId, page = 1)
                    _uiState.value = SeasonDetailUiState.Success(
                        season = season,
                        isInWatchlist = false,
                        isLoadingEpisodes = true
                    )
                }
                .onFailure {
                    _uiState.value = SeasonDetailUiState.NotFound
                }
        }
    }

    private fun loadEpisodes(malId: Int, page: Int) {
        viewModelScope.launch {
            fetchEpisodesUseCase(malId = malId, page = page)
                .onSuccess { episodePage ->
                    _uiState.update { state ->
                        if (state is SeasonDetailUiState.Success) {
                            state.copy(
                                episodes = state.episodes + episodePage.episodes,
                                isLoadingEpisodes = false,
                                hasMoreEpisodes = episodePage.hasNextPage,
                                nextEpisodePage = episodePage.nextPage
                            )
                        } else state
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        if (state is SeasonDetailUiState.Success) {
                            state.copy(isLoadingEpisodes = false)
                        } else state
                    }
                }
        }
    }

    fun loadMoreEpisodes() {
        val state = _uiState.value
        if (state !is SeasonDetailUiState.Success) return
        if (state.isLoadingEpisodes || !state.hasMoreEpisodes) return

        _uiState.update { (it as? SeasonDetailUiState.Success)?.copy(isLoadingEpisodes = true) ?: it }
        loadEpisodes(malId = state.season.malId, page = state.nextEpisodePage)
    }

    fun updateEpisodeProgress(newEpisode: Int) {
        val state = _uiState.value
        if (state !is SeasonDetailUiState.Success) return

        val maxEpisode = state.season.episodeCount
        val clamped = newEpisode.coerceAtLeast(0).let { ep ->
            if (maxEpisode != null) ep.coerceAtMost(maxEpisode) else ep
        }

        viewModelScope.launch {
            updateSeasonProgressUseCase(state.season, clamped)
        }
    }
}
