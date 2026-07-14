package com.vuzeda.animewatchlist.tracker.module.ui.screens.developer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeProvider
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.ui.R
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAnimeProviderUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAnimeUpdateSchedulerStateUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveIsNotificationDebugInfoEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetAnimeProviderUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetIsDeveloperOptionsEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetIsNotificationDebugInfoEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ShowAnimeUpdateNotificationUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.TriggerAnimeUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeAnimeUpdateSchedulerStateUseCase: ObserveAnimeUpdateSchedulerStateUseCase,
    private val observeIsNotificationDebugInfoEnabledUseCase: ObserveIsNotificationDebugInfoEnabledUseCase,
    private val observeAnimeProviderUseCase: ObserveAnimeProviderUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val setAnimeProviderUseCase: SetAnimeProviderUseCase,
    private val setIsDeveloperOptionsEnabledUseCase: SetIsDeveloperOptionsEnabledUseCase,
    private val setIsNotificationDebugInfoEnabledUseCase: SetIsNotificationDebugInfoEnabledUseCase,
    private val showAnimeUpdateNotificationUseCase: ShowAnimeUpdateNotificationUseCase,
    private val triggerAnimeUpdateUseCase: TriggerAnimeUpdateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeveloperUiState())
    val uiState: StateFlow<DeveloperUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeAnimeUpdateSchedulerStateUseCase(),
                observeIsNotificationDebugInfoEnabledUseCase(),
                observeAnimeProviderUseCase()
            ) { schedulerState, isNotificationDebugInfoEnabled, animeProvider ->
                Triple(schedulerState, isNotificationDebugInfoEnabled, animeProvider)
            }.collect { (schedulerState, isNotificationDebugInfoEnabled, animeProvider) ->
                _uiState.update {
                    it.copy(
                        lastAnimeUpdateRun = schedulerState.lastSuccessfulRunAt,
                        lastAnimeUpdateAttemptAt = schedulerState.lastAttemptAt,
                        lastAnimeUpdateAttemptResult = schedulerState.lastAttemptResult,
                        isNotificationDebugInfoEnabled = isNotificationDebugInfoEnabled,
                        animeProvider = animeProvider
                    )
                }
            }
        }
    }

    fun triggerAnimeUpdate() = triggerAnimeUpdateUseCase()

    fun triggerNewEpisodesTestNotification() {
        viewModelScope.launch {
            showAnimeUpdateNotificationUseCase(
                update = AnimeUpdate.NewEpisodes(
                    anime = Anime(
                        id = 0,
                        title = context.getString(R.string.developer_test_notification_anime_title)
                    ),
                    season = Season(
                        malId = 0,
                        title = context.getString(R.string.developer_test_notification_season_title)
                    ),
                    newEpisodeCount = 3
                ),
                titleLanguage = observeTitleLanguageUseCase().first()
            )
        }
    }

    fun triggerNewSeasonTestNotification() {
        viewModelScope.launch {
            showAnimeUpdateNotificationUseCase(
                update = AnimeUpdate.NewSeason(
                    anime = Anime(
                        id = 0,
                        title = context.getString(R.string.developer_test_notification_anime_title)
                    ),
                    sequelMalId = 0,
                    sequelTitle = context.getString(R.string.developer_test_notification_season_title)
                ),
                titleLanguage = observeTitleLanguageUseCase().first()
            )
        }
    }

    fun disableDeveloperOptions() {
        viewModelScope.launch { setIsDeveloperOptionsEnabledUseCase(false) }
    }

    fun toggleNotificationDebugInfo() {
        val enabled = _uiState.value.isNotificationDebugInfoEnabled
        viewModelScope.launch { setIsNotificationDebugInfoEnabledUseCase(!enabled) }
    }

    fun setAnimeProvider(provider: AnimeProvider) {
        viewModelScope.launch { setAnimeProviderUseCase(provider) }
    }
}
