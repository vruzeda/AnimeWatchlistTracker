package com.vuzeda.animewatchlist.tracker.module.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsEvent
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteAllDataUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveIsDeveloperOptionsEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetIsDeveloperOptionsEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetTitleLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deleteAllDataUseCase: DeleteAllDataUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val setTitleLanguageUseCase: SetTitleLanguageUseCase,
    private val observeHomeViewModeUseCase: ObserveHomeViewModeUseCase,
    private val setHomeViewModeUseCase: SetHomeViewModeUseCase,
    private val observeIsDeveloperOptionsEnabledUseCase: ObserveIsDeveloperOptionsEnabledUseCase,
    private val setIsDeveloperOptionsEnabledUseCase: SetIsDeveloperOptionsEnabledUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeTitleLanguageUseCase(),
                observeHomeViewModeUseCase(),
                observeIsDeveloperOptionsEnabledUseCase()
            ) { titleLanguage, homeViewMode, isDeveloperOptionsEnabled ->
                Triple(titleLanguage, homeViewMode, isDeveloperOptionsEnabled)
            }.collect { (titleLanguage, homeViewMode, isDeveloperOptionsEnabled) ->
                _uiState.update {
                    it.copy(
                        titleLanguage = titleLanguage,
                        homeViewMode = homeViewMode,
                        isDeveloperOptionsEnabled = isDeveloperOptionsEnabled
                    )
                }
            }
        }
    }

    fun setTitleLanguage(language: TitleLanguage) {
        viewModelScope.launch {
            setTitleLanguageUseCase(language)
            analyticsTracker.track(AnalyticsEvent.SetTitleLanguage(language.name))
        }
    }

    fun setHomeViewMode(mode: HomeViewMode) {
        viewModelScope.launch {
            setHomeViewModeUseCase(mode)
            analyticsTracker.track(AnalyticsEvent.SetHomeViewMode(mode.name))
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
            analyticsTracker.track(AnalyticsEvent.DeleteAllData)
            _uiState.update { it.copy(isDataDeleted = true) }
        }
    }

    fun showFeedbackSheet() {
        _uiState.update { it.copy(isFeedbackSheetVisible = true) }
    }

    fun hideFeedbackSheet() {
        _uiState.update { it.copy(isFeedbackSheetVisible = false) }
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
        if (newCount >= 5) {
            viewModelScope.launch { setIsDeveloperOptionsEnabledUseCase(true) }
        }
    }
}
