package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAnimeFromWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetAnimeByIdUseCase
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
    private val getAnimeByIdUseCase: GetAnimeByIdUseCase,
    private val updateAnimeUseCase: UpdateAnimeUseCase,
    private val deleteAnimeFromWatchlistUseCase: DeleteAnimeFromWatchlistUseCase
) : ViewModel() {

    private val animeId: Long = checkNotNull(savedStateHandle[Route.Detail.ARG_ANIME_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadAnime()
    }

    private fun loadAnime() {
        viewModelScope.launch {
            val anime = getAnimeByIdUseCase(animeId)
            _uiState.value = if (anime != null) {
                DetailUiState.Success(anime = anime)
            } else {
                DetailUiState.NotFound
            }
        }
    }

    fun toggleEditing() {
        _uiState.update { state ->
            if (state is DetailUiState.Success) {
                state.copy(isEditing = !state.isEditing)
            } else state
        }
    }

    fun updateStatus(status: WatchStatus) {
        _uiState.update { state ->
            if (state is DetailUiState.Success) {
                state.copy(editStatus = status)
            } else state
        }
    }

    fun updateCurrentEpisode(episode: Int) {
        _uiState.update { state ->
            if (state is DetailUiState.Success) {
                state.copy(editCurrentEpisode = episode.coerceAtLeast(0))
            } else state
        }
    }

    fun updateUserRating(rating: Int) {
        _uiState.update { state ->
            if (state is DetailUiState.Success) {
                state.copy(editUserRating = rating)
            } else state
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        viewModelScope.launch {
            val updatedAnime = state.anime.copy(
                status = state.editStatus,
                currentEpisode = state.editCurrentEpisode,
                userRating = if (state.editUserRating > 0) state.editUserRating else null
            )
            updateAnimeUseCase(updatedAnime)
            _uiState.value = DetailUiState.Success(
                anime = updatedAnime,
                isEditing = false
            )
        }
    }

    fun deleteAnime(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteAnimeFromWatchlistUseCase(animeId)
            onDeleted()
        }
    }
}
