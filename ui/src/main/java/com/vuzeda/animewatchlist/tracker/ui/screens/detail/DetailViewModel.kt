package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAnimeUseCase
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
    private val updateAnimeUseCase: UpdateAnimeUseCase,
    private val deleteAnimeUseCase: DeleteAnimeUseCase,
    private val toggleAnimeNotificationsUseCase: ToggleAnimeNotificationsUseCase
) : ViewModel() {

    private val animeId: Long = checkNotNull(savedStateHandle[Route.Detail.ARG_ANIME_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        if (animeId > 0) {
            observeAnime()
        } else {
            _uiState.value = DetailUiState.NotFound
        }
    }

    private fun observeAnime() {
        viewModelScope.launch {
            observeAnimeByIdUseCase(animeId).collect { anime ->
                if (anime != null) {
                    _uiState.update { currentState ->
                        when (currentState) {
                            is DetailUiState.Success -> currentState.copy(
                                anime = anime,
                                isNotificationsEnabled = anime.isNotificationsEnabled
                            )
                            else -> DetailUiState.Success(anime = anime)
                        }
                    }
                } else {
                    _uiState.value = DetailUiState.NotFound
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

    fun updateStatus(status: WatchStatus) {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        viewModelScope.launch {
            updateAnimeUseCase(state.anime.copy(status = status))
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
            deleteAnimeUseCase(animeId)
            onDeleted()
        }
    }

    fun toggleNotifications() {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        val newEnabled = !state.anime.isNotificationsEnabled
        viewModelScope.launch {
            toggleAnimeNotificationsUseCase(id = animeId, enabled = newEnabled)
        }
    }
}
