package com.vuzeda.animewatchlist.tracker.ui.screens.animedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FindAnimeBySeasonMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeByIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ResolveAnimeProgressivelyUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ToggleAnimeNotificationsUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeAnimeByIdUseCase: ObserveAnimeByIdUseCase,
    private val observeSeasonsForAnimeUseCase: ObserveSeasonsForAnimeUseCase,
    private val updateAnimeUseCase: UpdateAnimeUseCase,
    private val deleteAnimeUseCase: DeleteAnimeUseCase,
    private val toggleAnimeNotificationsUseCase: ToggleAnimeNotificationsUseCase,
    private val resolveAnimeProgressivelyUseCase: ResolveAnimeProgressivelyUseCase,
    private val addAnimeUseCase: AddAnimeUseCase,
    private val findAnimeBySeasonMalIdUseCase: FindAnimeBySeasonMalIdUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase
) : ViewModel() {

    private val animeId: Long = checkNotNull(savedStateHandle[Route.AnimeDetail.ARG_ANIME_ID])
    private val malId: Int = savedStateHandle[Route.AnimeDetail.ARG_MAL_ID] ?: 0

    private val _uiState = MutableStateFlow<AnimeDetailUiState>(AnimeDetailUiState.Loading)
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    private var resolvedAnime: Anime? = null
    private var resolvedSeasons: List<Season> = emptyList()

    init {
        observeTitleLanguage()
        if (animeId > 0) {
            observeAnime(animeId)
        } else if (malId > 0) {
            resolveFromApi()
        } else {
            _uiState.value = AnimeDetailUiState.NotFound
        }
    }

    private fun observeTitleLanguage() {
        viewModelScope.launch {
            observeTitleLanguageUseCase().collect { titleLanguage ->
                _uiState.update { state ->
                    when (state) {
                        is AnimeDetailUiState.Success -> state.copy(titleLanguage = titleLanguage)
                        else -> state
                    }
                }
            }
        }
    }

    private fun observeAnime(id: Long) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                observeAnimeByIdUseCase(id),
                observeSeasonsForAnimeUseCase(id)
            ) { anime, seasons ->
                anime to seasons
            }.collect { (anime, seasons) ->
                if (anime != null) {
                    _uiState.update { currentState ->
                        when (currentState) {
                            is AnimeDetailUiState.Success -> currentState.copy(
                                anime = anime,
                                seasons = seasons,
                                isInWatchlist = true,
                                isNotificationsEnabled = anime.isNotificationsEnabled
                            )
                            else -> AnimeDetailUiState.Success(
                                anime = anime,
                                seasons = seasons,
                                isInWatchlist = true
                            )
                        }
                    }
                } else {
                    _uiState.value = AnimeDetailUiState.NotFound
                }
            }
        }
    }

    private fun resolveFromApi() {
        viewModelScope.launch {
            val existingAnimeId = findAnimeBySeasonMalIdUseCase(malId)
            if (existingAnimeId != null) {
                observeAnime(existingAnimeId)
                return@launch
            }

            resolveAnimeProgressivelyUseCase(malId)
                .catch { _uiState.value = AnimeDetailUiState.NotFound }
                .collect { result ->
                    val anime = Anime(
                        title = result.title,
                        titleEnglish = result.titleEnglish,
                        titleJapanese = result.titleJapanese,
                        imageUrl = result.imageUrl,
                        synopsis = result.synopsis,
                        genres = result.genres
                    )
                    val seasons = result.seasons.mapIndexed { index, seasonData ->
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
                    resolvedAnime = anime
                    resolvedSeasons = seasons
                    _uiState.update { currentState ->
                        when (currentState) {
                            is AnimeDetailUiState.Success -> currentState.copy(
                                anime = anime,
                                seasons = seasons,
                                isResolvingPrequels = result.isResolvingPrequels,
                                isResolvingSequels = result.isResolvingSequels
                            )
                            else -> AnimeDetailUiState.Success(
                                anime = anime,
                                seasons = seasons,
                                isInWatchlist = false,
                                isResolvingPrequels = result.isResolvingPrequels,
                                isResolvingSequels = result.isResolvingSequels
                            )
                        }
                    }
                }
        }
    }

    fun showStatusSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isStatusSheetVisible = true)
            else state
        }
    }

    fun dismissStatusSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isStatusSheetVisible = false)
            else state
        }
    }

    fun showAddSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isAddSheetVisible = true)
            else state
        }
    }

    fun dismissAddSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isAddSheetVisible = false)
            else state
        }
    }

    fun addToWatchlist(status: WatchStatus) {
        val anime = resolvedAnime ?: return
        val seasons = resolvedSeasons

        viewModelScope.launch {
            val newId = addAnimeUseCase(
                anime = anime,
                seasons = seasons,
                status = status
            )
            resolvedAnime = null
            resolvedSeasons = emptyList()
            observeAnime(newId)
        }
    }

    fun updateStatus(status: WatchStatus) {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return

        viewModelScope.launch {
            updateAnimeUseCase(state.anime.copy(status = status))
        }
    }

    fun updateUserRating(rating: Int) {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return

        viewModelScope.launch {
            updateAnimeUseCase(state.anime.copy(userRating = if (rating > 0) rating else null))
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isDeleteConfirmationVisible = true)
            else state
        }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isDeleteConfirmationVisible = false)
            else state
        }
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return

        viewModelScope.launch {
            deleteAnimeUseCase(state.anime.id)
            onDeleted()
        }
    }

    fun toggleNotifications() {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return

        val newEnabled = !state.anime.isNotificationsEnabled
        viewModelScope.launch {
            toggleAnimeNotificationsUseCase(id = state.anime.id, enabled = newEnabled)
        }
    }

    fun clearSnackbar() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(snackbarMessage = null)
            else state
        }
    }
}
