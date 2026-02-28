package com.vuzeda.animewatchlist.tracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeWatchlistUseCase: ObserveWatchlistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeWatchlist(status = null)
    }

    fun selectTab(status: WatchStatus?) {
        _uiState.update { it.copy(selectedTab = status, isLoading = true) }
        observeWatchlist(status)
    }

    private fun observeWatchlist(status: WatchStatus?) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            observeWatchlistUseCase(status).collect { animeList ->
                _uiState.update { it.copy(animeList = animeList, isLoading = false) }
            }
        }
    }
}
