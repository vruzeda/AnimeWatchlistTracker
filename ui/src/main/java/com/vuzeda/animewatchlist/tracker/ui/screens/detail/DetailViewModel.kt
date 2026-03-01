package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.RelatedAnime
import com.vuzeda.animewatchlist.tracker.domain.model.RelationType
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAnimeFromWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchAnimeByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeByIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ToggleAnimeNotificationsUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeAnimeByIdUseCase: ObserveAnimeByIdUseCase,
    private val fetchAnimeByMalIdUseCase: FetchAnimeByMalIdUseCase,
    private val updateAnimeUseCase: UpdateAnimeUseCase,
    private val deleteAnimeFromWatchlistUseCase: DeleteAnimeFromWatchlistUseCase,
    private val toggleAnimeNotificationsUseCase: ToggleAnimeNotificationsUseCase,
    private val addAnimeToWatchlistUseCase: AddAnimeToWatchlistUseCase,
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private var currentAnimeId: Long = checkNotNull(savedStateHandle[Route.Detail.ARG_ANIME_ID])
    private val malId: Int = savedStateHandle[Route.Detail.ARG_MAL_ID] ?: 0
    private var nextEpisodePage: Int = 1

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        if (currentAnimeId > 0) {
            observeAnime()
        } else if (malId > 0) {
            fetchAnimeFromApi()
        } else {
            _uiState.value = DetailUiState.NotFound
        }
    }

    private fun observeAnime() {
        viewModelScope.launch {
            observeAnimeByIdUseCase(currentAnimeId).collect { anime ->
                if (anime != null) {
                    val isFirstLoad = _uiState.value !is DetailUiState.Success
                    _uiState.update { currentState ->
                        when (currentState) {
                            is DetailUiState.Success -> currentState.copy(
                                anime = anime,
                                isInWatchlist = true,
                                isNotificationsEnabled = anime.isNotificationsEnabled
                            )
                            else -> DetailUiState.Success(anime = anime, isInWatchlist = true)
                        }
                    }
                    if (isFirstLoad) {
                        anime.malId?.let { fetchExtras(it) }
                    }
                } else {
                    _uiState.value = DetailUiState.NotFound
                }
            }
        }
    }

    private fun fetchAnimeFromApi() {
        viewModelScope.launch {
            fetchAnimeByMalIdUseCase(malId)
                .onSuccess { anime ->
                    _uiState.value = DetailUiState.Success(
                        anime = anime,
                        isInWatchlist = false
                    )
                    fetchExtras(malId)
                }
                .onFailure {
                    _uiState.value = DetailUiState.NotFound
                }
        }
    }

    private fun fetchExtras(malId: Int) {
        fetchEpisodes(malId)
        fetchRelatedAnime(malId)
    }

    private fun fetchEpisodes(malId: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                if (state is DetailUiState.Success) state.copy(isLoadingEpisodes = true)
                else state
            }
            animeRepository.fetchAnimeEpisodes(malId = malId, page = 1)
                .onSuccess { page ->
                    nextEpisodePage = page.nextPage
                    _uiState.update { state ->
                        if (state is DetailUiState.Success) state.copy(
                            episodes = page.episodes,
                            isLoadingEpisodes = false,
                            hasMoreEpisodes = page.hasNextPage
                        )
                        else state
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        if (state is DetailUiState.Success) state.copy(isLoadingEpisodes = false)
                        else state
                    }
                }
        }
    }

    fun loadMoreEpisodes() {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return
        val animeMalId = state.anime.malId ?: return
        if (state.isLoadingEpisodes || !state.hasMoreEpisodes) return

        _uiState.update { (it as? DetailUiState.Success)?.copy(isLoadingEpisodes = true) ?: it }
        viewModelScope.launch {
            animeRepository.fetchAnimeEpisodes(malId = animeMalId, page = nextEpisodePage)
                .onSuccess { page ->
                    nextEpisodePage = page.nextPage
                    _uiState.update { s ->
                        if (s is DetailUiState.Success) s.copy(
                            episodes = s.episodes + page.episodes,
                            isLoadingEpisodes = false,
                            hasMoreEpisodes = page.hasNextPage
                        )
                        else s
                    }
                }
                .onFailure {
                    _uiState.update { s ->
                        if (s is DetailUiState.Success) s.copy(isLoadingEpisodes = false)
                        else s
                    }
                }
        }
    }

    private fun fetchRelatedAnime(malId: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                if (state is DetailUiState.Success) state.copy(isLoadingRelated = true)
                else state
            }
            animeRepository.fetchAnimeFullDetails(malId)
                .onSuccess { details ->
                    val related = details.prequels.map {
                        RelatedAnime(
                            malId = it.malId,
                            title = it.title,
                            relationType = RelationType.PREQUEL
                        )
                    } + details.sequels.map {
                        RelatedAnime(
                            malId = it.malId,
                            title = it.title,
                            relationType = RelationType.SEQUEL
                        )
                    }
                    _uiState.update { state ->
                        if (state is DetailUiState.Success) state.copy(
                            relatedAnime = related,
                            isLoadingRelated = false
                        )
                        else state
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        if (state is DetailUiState.Success) state.copy(isLoadingRelated = false)
                        else state
                    }
                }
        }
    }

    fun showStatusSheet() {
        _uiState.update { state ->
            if (state is DetailUiState.Success) state.copy(isStatusSheetVisible = true)
            else state
        }
    }

    fun dismissStatusSheet() {
        _uiState.update { state ->
            if (state is DetailUiState.Success) state.copy(isStatusSheetVisible = false)
            else state
        }
    }

    fun showAddSheet() {
        _uiState.update { state ->
            if (state is DetailUiState.Success) state.copy(isAddSheetVisible = true)
            else state
        }
    }

    fun dismissAddSheet() {
        _uiState.update { state ->
            if (state is DetailUiState.Success) state.copy(isAddSheetVisible = false)
            else state
        }
    }

    fun addToWatchlist(status: WatchStatus) {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        val animeWithStatus = state.anime.copy(status = status)
        _uiState.update { (it as? DetailUiState.Success)?.copy(isAddSheetVisible = false) ?: it }

        viewModelScope.launch {
            val newId = addAnimeToWatchlistUseCase(animeWithStatus)
            currentAnimeId = newId
            observeAnime()
        }
    }

    fun updateStatus(status: WatchStatus) {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        viewModelScope.launch {
            updateAnimeUseCase(state.anime.copy(status = status))
        }
    }

    fun updateCurrentEpisode(episode: Int) {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        val clamped = episode.coerceAtLeast(0)
        viewModelScope.launch {
            updateAnimeUseCase(state.anime.copy(currentEpisode = clamped))
        }
    }

    fun updateUserRating(rating: Int) {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        viewModelScope.launch {
            updateAnimeUseCase(state.anime.copy(userRating = if (rating > 0) rating else null))
        }
    }

    fun deleteAnime(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteAnimeFromWatchlistUseCase(currentAnimeId)
            onDeleted()
        }
    }

    fun toggleNotifications() {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        val newEnabled = !state.anime.isNotificationsEnabled
        viewModelScope.launch {
            toggleAnimeNotificationsUseCase(id = currentAnimeId, enabled = newEnabled)
        }
    }
}
