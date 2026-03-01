package com.vuzeda.animewatchlist.tracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAllDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deleteAllDataUseCase: DeleteAllDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
}
