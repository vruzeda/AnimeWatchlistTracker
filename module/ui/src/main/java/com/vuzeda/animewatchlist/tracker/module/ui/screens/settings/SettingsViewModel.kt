package com.vuzeda.animewatchlist.tracker.module.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteAllDataUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetTitleLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deleteAllDataUseCase: DeleteAllDataUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val setTitleLanguageUseCase: SetTitleLanguageUseCase,
    private val observeHomeViewModeUseCase: ObserveHomeViewModeUseCase,
    private val setHomeViewModeUseCase: SetHomeViewModeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTitleLanguageUseCase().collect { titleLanguage ->
                _uiState.update { it.copy(titleLanguage = titleLanguage) }
            }
        }
        viewModelScope.launch {
            observeHomeViewModeUseCase().collect { viewMode ->
                _uiState.update { it.copy(homeViewMode = viewMode) }
            }
        }
    }

    fun setTitleLanguage(language: TitleLanguage) {
        viewModelScope.launch {
            setTitleLanguageUseCase(language)
        }
    }

    fun setHomeViewMode(mode: HomeViewMode) {
        viewModelScope.launch {
            setHomeViewModeUseCase(mode)
        }
    }

    fun requestDeleteAllData() {
        _uiState.update { it.copy(isDeleteConfirmationVisible = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(isDeleteConfirmationVisible = false) }
    }

    fun confirmDeleteAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleteConfirmationVisible = false) }
            deleteAllDataUseCase()
            _uiState.update { it.copy(isDataDeleted = true) }
        }
    }

    fun clearDataDeletedFlag() {
        _uiState.update { it.copy(isDataDeleted = false) }
    }

    fun onVersionTap() {
        val current = _uiState.value
        if (current.isDeveloperOptionsEnabled) return
        val newCount = current.developerTapCount + 1
        _uiState.update {
            it.copy(
                developerTapCount = newCount,
                isDeveloperOptionsEnabled = newCount >= 5
            )
        }
    }
}
