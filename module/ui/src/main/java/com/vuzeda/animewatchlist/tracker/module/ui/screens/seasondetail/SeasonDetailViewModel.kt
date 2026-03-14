package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeFromDetailsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteSeasonUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FindSeasonIdByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonByIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ToggleSeasonEpisodeNotificationsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.UpdateSeasonProgressUseCase
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
    private val observeSeasonsForAnimeUseCase: ObserveSeasonsForAnimeUseCase,
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase,
    private val fetchEpisodesUseCase: FetchEpisodesUseCase,
    private val updateSeasonProgressUseCase: UpdateSeasonProgressUseCase,
    private val deleteSeasonUseCase: DeleteSeasonUseCase,
    private val addAnimeFromDetailsUseCase: AddAnimeFromDetailsUseCase,
    private val findSeasonIdByMalIdUseCase: FindSeasonIdByMalIdUseCase,
    private val toggleSeasonEpisodeNotificationsUseCase: ToggleSeasonEpisodeNotificationsUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase
) : ViewModel() {

    private val seasonId: Long = checkNotNull(savedStateHandle["seasonId"])
    private val malId: Int = savedStateHandle["malId"] ?: 0

    private var pendingDetails: AnimeFullDetails? = null
    private var observingSiblingsForAnimeId: Long = -1L

    private val _uiState = MutableStateFlow<SeasonDetailUiState>(SeasonDetailUiState.Loading)
    val uiState: StateFlow<SeasonDetailUiState> = _uiState.asStateFlow()

    init {
        observeTitleLanguage()
        if (seasonId > 0) {
            observeSeason()
        } else if (malId > 0) {
            loadFromApi()
        } else {
            _uiState.value = SeasonDetailUiState.NotFound
        }
    }

    private fun observeSiblingCount(animeId: Long) {
        if (observingSiblingsForAnimeId == animeId) return
        observingSiblingsForAnimeId = animeId
        viewModelScope.launch {
            observeSeasonsForAnimeUseCase(animeId).collect { siblings ->
                _uiState.update { state ->
                    if (state is SeasonDetailUiState.Success) state.copy(isLastSeason = siblings.size <= 1)
                    else state
                }
            }
        }
    }

    private fun observeTitleLanguage() {
        viewModelScope.launch {
            observeTitleLanguageUseCase().collect { titleLanguage ->
                _uiState.update { state ->
                    when (state) {
                        is SeasonDetailUiState.Success -> state.copy(titleLanguage = titleLanguage)
                        else -> state
                    }
                }
            }
        }
    }

    private fun observeSeason() {
        viewModelScope.launch {
            observeSeasonByIdUseCase(seasonId).collect { season ->
                if (season != null) {
                    observeSiblingCount(season.animeId)
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
            val existingSeasonId = findSeasonIdByMalIdUseCase(malId)
            if (existingSeasonId != null) {
                observeSeason(existingSeasonId)
                return@launch
            }

            fetchSeasonDetailUseCase(malId)
                .onSuccess { details ->
                    pendingDetails = details
                    val season = Season(
                        malId = details.malId,
                        title = details.title,
                        titleEnglish = details.titleEnglish,
                        titleJapanese = details.titleJapanese,
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

    fun showDeleteConfirmation() {
        _uiState.update { state ->
            if (state is SeasonDetailUiState.Success) state.copy(isDeleteConfirmationVisible = true)
            else state
        }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { state ->
            if (state is SeasonDetailUiState.Success) state.copy(isDeleteConfirmationVisible = false)
            else state
        }
    }

    fun confirmDelete() {
        val state = _uiState.value
        if (state !is SeasonDetailUiState.Success) return

        viewModelScope.launch {
            deleteSeasonUseCase(state.season)
            _uiState.update { current ->
                if (current is SeasonDetailUiState.Success) current.copy(isDeleted = true)
                else current
            }
        }
    }

    fun showAddSheet() {
        _uiState.update { state ->
            if (state is SeasonDetailUiState.Success) state.copy(isAddSheetVisible = true)
            else state
        }
    }

    fun dismissAddSheet() {
        _uiState.update { state ->
            if (state is SeasonDetailUiState.Success) state.copy(isAddSheetVisible = false)
            else state
        }
    }

    fun addToWatchlist(status: WatchStatus) {
        val details = pendingDetails ?: return

        viewModelScope.launch {
            addAnimeFromDetailsUseCase(details, status)

            pendingDetails = null
            _uiState.update { state ->
                if (state is SeasonDetailUiState.Success) state.copy(
                    isAddSheetVisible = false,
                    snackbarEvent = SeasonDetailSnackbarEvent.AddedToWatchlist(details.title)
                )
                else state
            }

            val addedSeasonId = findSeasonIdByMalIdUseCase(malId)
            if (addedSeasonId != null) {
                observeSeason(addedSeasonId)
            }
        }
    }

    fun toggleEpisodeNotifications() {
        val state = _uiState.value
        if (state !is SeasonDetailUiState.Success) return

        val newEnabled = !state.isEpisodeNotificationsEnabled
        viewModelScope.launch {
            toggleSeasonEpisodeNotificationsUseCase(
                seasonId = state.season.id,
                enabled = newEnabled
            )
            _uiState.update { current ->
                if (current is SeasonDetailUiState.Success) current.copy(
                    isEpisodeNotificationsEnabled = newEnabled,
                    snackbarEvent = SeasonDetailSnackbarEvent.EpisodeNotificationsToggled(newEnabled)
                )
                else current
            }
        }
    }

    fun navigateToAnimeDetail() {
        val state = _uiState.value
        if (state !is SeasonDetailUiState.Success) return

        _uiState.update { current ->
            if (current is SeasonDetailUiState.Success) current.copy(
                pendingNavigationMalId = state.season.malId
            )
            else current
        }
    }

    fun onNavigated() {
        _uiState.update { state ->
            if (state is SeasonDetailUiState.Success) state.copy(pendingNavigationMalId = null)
            else state
        }
    }

    fun clearSnackbar() {
        _uiState.update { state ->
            if (state is SeasonDetailUiState.Success) state.copy(snackbarEvent = null)
            else state
        }
    }

    private fun observeSeason(seasonId: Long) {
        viewModelScope.launch {
            observeSeasonByIdUseCase(seasonId).collect { season ->
                if (season != null) {
                    observeSiblingCount(season.animeId)
                    _uiState.update { currentState ->
                        when (currentState) {
                            is SeasonDetailUiState.Success -> currentState.copy(
                                season = season,
                                isInWatchlist = true,
                                isEpisodeNotificationsEnabled = season.isEpisodeNotificationsEnabled
                            )
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
}
