package com.vuzeda.animewatchlist.tracker.module.ui.screens.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveLastAnimeUpdateRunUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetIsDeveloperOptionsEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.TriggerAnimeUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val observeLastAnimeUpdateRunUseCase: ObserveLastAnimeUpdateRunUseCase,
    private val triggerAnimeUpdateUseCase: TriggerAnimeUpdateUseCase,
    private val setIsDeveloperOptionsEnabledUseCase: SetIsDeveloperOptionsEnabledUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeveloperUiState())
    val uiState: StateFlow<DeveloperUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeLastAnimeUpdateRunUseCase().collect { instant ->
                _uiState.update { it.copy(lastAnimeUpdateRun = instant) }
            }
        }
    }

    fun triggerAnimeUpdate() = triggerAnimeUpdateUseCase()

    fun disableDeveloperOptions() {
        viewModelScope.launch { setIsDeveloperOptionsEnabledUseCase(false) }
    }
}
