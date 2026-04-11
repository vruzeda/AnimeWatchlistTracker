package com.vuzeda.animewatchlist.tracker.module.ui.screens.settings

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsEvent
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.domain.Feedback
import com.vuzeda.animewatchlist.tracker.module.domain.FeedbackCategory
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SubmitFeedbackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val observeHomeViewModeUseCase: ObserveHomeViewModeUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    private var titleLanguage: TitleLanguage = TitleLanguage.DEFAULT
    private var homeViewMode: HomeViewMode = HomeViewMode.ANIME

    init {
        viewModelScope.launch {
            observeTitleLanguageUseCase().collect { titleLanguage = it }
        }
        viewModelScope.launch {
            observeHomeViewModeUseCase().collect { homeViewMode = it }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    fun submitFeedback() {
        val state = _uiState.value
        val category = state.category?.let { runCatching { FeedbackCategory.valueOf(it) }.getOrNull() }
            ?: return
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val feedback = Feedback(
                category = category,
                message = state.message,
                appVersion = resolveAppVersion(),
                timestamp = System.currentTimeMillis(),
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                androidVersion = Build.VERSION.SDK_INT,
                installationId = "",
                titleLanguage = titleLanguage.name,
                homeViewMode = homeViewMode.name
            )
            submitFeedbackUseCase(feedback)
                .onSuccess {
                    analyticsTracker.track(AnalyticsEvent.SubmitFeedback(category.name))
                    _uiState.update { it.copy(isSubmitting = false, snackbarEvent = FeedbackSnackbarEvent.Success) }
                }
                .onFailure {
                    _uiState.update { it.copy(isSubmitting = false, snackbarEvent = FeedbackSnackbarEvent.Error) }
                }
        }
    }

    fun clearSnackbarEvent() {
        _uiState.update { it.copy(snackbarEvent = null) }
    }

    fun reset() {
        _uiState.update { FeedbackUiState() }
    }

    @Suppress("DEPRECATION")
    private fun resolveAppVersion(): String {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            info.versionCode.toLong()
        }
        return "${info.versionName} ($versionCode)"
    }
}
