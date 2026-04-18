package com.vuzeda.animewatchlist.tracker.module.ui.screens.animedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsEvent
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.AddSeasonToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FindAnimeBySeasonMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAnimeByIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveIsNotificationDebugInfoEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RefreshAnimeSeasonsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RefreshSeasonDataUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ResolveAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ToggleAnimeNotificationsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.UpdateSeasonStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeAnimeByIdUseCase: ObserveAnimeByIdUseCase,
    private val observeSeasonsForAnimeUseCase: ObserveSeasonsForAnimeUseCase,
    private val updateAnimeUseCase: UpdateAnimeUseCase,
    private val updateSeasonStatusUseCase: UpdateSeasonStatusUseCase,
    private val deleteAnimeUseCase: DeleteAnimeUseCase,
    private val toggleAnimeNotificationsUseCase: ToggleAnimeNotificationsUseCase,
    private val resolveAnimeUseCase: ResolveAnimeUseCase,
    private val addAnimeUseCase: AddAnimeUseCase,
    private val addSeasonToWatchlistUseCase: AddSeasonToWatchlistUseCase,
    private val findAnimeBySeasonMalIdUseCase: FindAnimeBySeasonMalIdUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val refreshAnimeSeasonsUseCase: RefreshAnimeSeasonsUseCase,
    private val refreshSeasonDataUseCase: RefreshSeasonDataUseCase,
    private val observeIsNotificationDebugInfoEnabledUseCase: ObserveIsNotificationDebugInfoEnabledUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val animeId: Long = checkNotNull(savedStateHandle["animeId"])
    private val malId: Int = savedStateHandle["malId"] ?: 0

    private val _uiState = MutableStateFlow(AnimeDetailUiState())
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        if (animeId > 0) {
            observeAnime(animeId)
        } else if (malId > 0) {
            resolveFromApi()
        } else {
            _uiState.update { it.copy(isLoading = false, isNotFound = true) }
        }
    }

    private fun observeAnime(id: Long) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                observeAnimeByIdUseCase(id),
                observeSeasonsForAnimeUseCase(id),
                observeTitleLanguageUseCase(),
                observeIsNotificationDebugInfoEnabledUseCase()
            ) { anime, seasons, titleLanguage, isNotificationDebugInfoEnabled ->
                AnimeCombinedData(anime, seasons, titleLanguage, isNotificationDebugInfoEnabled)
            }.collect { (anime, seasons, titleLanguage, isNotificationDebugInfoEnabled) ->
                if (anime != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isNotFound = false,
                            anime = anime,
                            seasons = seasons,
                            isInWatchlist = true,
                            notificationType = anime.notificationType,
                            titleLanguage = titleLanguage,
                            isNotificationDebugInfoEnabled = isNotificationDebugInfoEnabled
                        )
                    }
                } else {
                    _uiState.update { current ->
                        if (current.anime != null && !current.isInWatchlist) current
                        else current.copy(isLoading = false, isNotFound = true, anime = null)
                    }
                }
            }
        }
        viewModelScope.launch {
            runCatching { refreshAnimeSeasonsUseCase(id) }
        }
        viewModelScope.launch {
            val seasons = observeSeasonsForAnimeUseCase(id).first()
            seasons.forEach { season ->
                launch { runCatching { refreshSeasonDataUseCase(season) } }
            }
        }
    }

    private fun resolveFromApi(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val existingAnimeId = findAnimeBySeasonMalIdUseCase(malId)
            if (existingAnimeId != null) {
                if (isRefresh) {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                observeAnime(existingAnimeId)
                return@launch
            }

            val titleLanguage = observeTitleLanguageUseCase().first()
            val isNotificationDebugInfoEnabled = observeIsNotificationDebugInfoEnabledUseCase().first()

            resolveAnimeUseCase(malId)
                .onSuccess { result ->
                    val anime = Anime(
                        title = result.title,
                        titleEnglish = result.titleEnglish,
                        titleJapanese = result.titleJapanese,
                        imageUrl = result.imageUrl,
                        synopsis = result.synopsis,
                        genres = result.genres
                    )
                    val seasons = result.seasons.mapIndexed { index, seasonData ->
                        Season(
                            malId = seasonData.malId,
                            title = seasonData.title,
                            titleEnglish = seasonData.titleEnglish,
                            titleJapanese = seasonData.titleJapanese,
                            imageUrl = seasonData.imageUrl,
                            type = seasonData.type,
                            episodeCount = seasonData.episodeCount,
                            score = seasonData.score,
                            airingStatus = seasonData.airingStatus,
                            orderIndex = index,
                            isInWatchlist = false
                        )
                    }
                    if (isRefresh) {
                        _uiState.update { it.copy(anime = anime, seasons = seasons, isRefreshing = false) }
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false,
                            isNotFound = false,
                            anime = anime,
                            seasons = seasons,
                            isInWatchlist = false,
                            titleLanguage = titleLanguage,
                            isNotificationDebugInfoEnabled = isNotificationDebugInfoEnabled
                        ) }
                        viewModelScope.launch {
                            observeTitleLanguageUseCase().collect { lang ->
                                _uiState.update { it.copy(titleLanguage = lang) }
                            }
                        }
                        viewModelScope.launch {
                            observeIsNotificationDebugInfoEnabledUseCase().collect { enabled ->
                                _uiState.update { it.copy(isNotificationDebugInfoEnabled = enabled) }
                            }
                        }
                    }
                }
                .onFailure {
                    if (isRefresh) {
                        _uiState.update { it.copy(isRefreshing = false) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, isNotFound = true) }
                    }
                }
        }
    }

    fun refresh() {
        if (_uiState.value.anime == null) return
        _uiState.update { it.copy(isRefreshing = true) }
        if (animeId > 0) {
            viewModelScope.launch {
                runCatching { refreshAnimeSeasonsUseCase(animeId) }
                val seasons = observeSeasonsForAnimeUseCase(animeId).first()
                seasons.forEach { season -> runCatching { refreshSeasonDataUseCase(season) } }
                _uiState.update { it.copy(isRefreshing = false) }
            }
        } else if (malId > 0) {
            resolveFromApi(isRefresh = true)
        }
    }

    fun showStatusSheet() {
        _uiState.update { it.copy(isStatusSheetVisible = true) }
    }

    fun dismissStatusSheet() {
        _uiState.update { it.copy(isStatusSheetVisible = false) }
    }

    fun showAddSheet() {
        _uiState.update { it.copy(isAddSheetVisible = true) }
    }

    fun dismissAddSheet() {
        _uiState.update { it.copy(isAddSheetVisible = false) }
    }

    fun showAddScopeSheet(status: WatchStatus) {
        _uiState.update { it.copy(
            isAddSheetVisible = false,
            isAddScopeSheetVisible = true,
            pendingAddStatus = status
        ) }
    }

    fun dismissAddScopeSheet() {
        _uiState.update { it.copy(
            isAddScopeSheetVisible = false,
            pendingAddStatus = null
        ) }
    }

    fun showAddSeasonSheet(season: Season) {
        _uiState.update { it.copy(
            isAddSeasonSheetVisible = true,
            pendingAddSeason = season
        ) }
    }

    fun dismissAddSeasonSheet() {
        _uiState.update { it.copy(
            isAddSeasonSheetVisible = false,
            pendingAddSeason = null
        ) }
    }

    fun confirmAddSeason(status: WatchStatus) {
        val state = _uiState.value
        val season = state.pendingAddSeason ?: return
        val anime = state.anime ?: return

        _uiState.update { it.copy(
            isAddSeasonSheetVisible = false,
            pendingAddSeason = null
        ) }

        viewModelScope.launch {
            if (season.id > 0) {
                addSeasonToWatchlistUseCase(season, status)
                analyticsTracker.track(AnalyticsEvent.AddSeason(status.name))
            } else {
                val newId = addAnimeUseCase(anime, listOf(season), status)
                analyticsTracker.track(AnalyticsEvent.AddSeason(status.name))
                _uiState.update { it.copy(
                    snackbarEvent = AnimeDetailSnackbarEvent.AddedToWatchlist(anime.title)
                ) }
                observeAnime(newId)
            }
        }
    }

    fun confirmAddScope(allSeasons: Boolean) {
        val state = _uiState.value
        val status = state.pendingAddStatus ?: return
        val anime = state.anime ?: return
        val seasons = if (allSeasons) state.seasons else state.seasons.take(1)
        val title = anime.title

        _uiState.update { it.copy(
            isAddScopeSheetVisible = false,
            pendingAddStatus = null
        ) }

        viewModelScope.launch {
            val newId = addAnimeUseCase(
                anime = anime,
                seasons = seasons,
                status = status
            )
            analyticsTracker.track(AnalyticsEvent.AddAnime(status.name, seasons.size, allSeasons))
            _uiState.update { it.copy(
                snackbarEvent = AnimeDetailSnackbarEvent.AddedToWatchlist(title)
            ) }
            observeAnime(newId)
        }
    }

    fun updateStatus(status: WatchStatus) {
        val state = _uiState.value
        if (state.anime == null) return
        val mostRecentSeason = state.seasons.maxByOrNull { it.orderIndex } ?: return

        viewModelScope.launch {
            updateSeasonStatusUseCase(mostRecentSeason, status)
            analyticsTracker.track(AnalyticsEvent.UpdateAnimeStatus(status.name))
        }
    }

    fun updateUserRating(rating: Int) {
        val state = _uiState.value
        val anime = state.anime ?: return

        viewModelScope.launch {
            updateAnimeUseCase(anime.copy(userRating = if (rating > 0) rating else null))
            analyticsTracker.track(AnalyticsEvent.UpdateUserRating(rating))
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(isDeleteConfirmationVisible = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(isDeleteConfirmationVisible = false) }
    }

    fun confirmDelete() {
        val anime = _uiState.value.anime ?: return

        viewModelScope.launch {
            deleteAnimeUseCase(anime.id)
            analyticsTracker.track(AnalyticsEvent.RemoveAnime(anime.status.name))
            _uiState.update { it.copy(
                isInWatchlist = false,
                isDeleteConfirmationVisible = false
            ) }
        }
    }

    fun onNotificationIconClick() {
        val state = _uiState.value
        if (state.anime == null) return

        if (state.isNotificationsEnabled) {
            viewModelScope.launch {
                toggleAnimeNotificationsUseCase(
                    id = state.anime.id,
                    notificationType = NotificationType.NONE
                )
                _uiState.update { it.copy(
                    snackbarEvent = AnimeDetailSnackbarEvent.NotificationsDisabled
                ) }
            }
        } else {
            _uiState.update { it.copy(isNotificationTypeSheetVisible = true) }
        }
    }

    fun dismissNotificationTypeSheet() {
        _uiState.update { it.copy(isNotificationTypeSheetVisible = false) }
    }

    fun selectNotificationType(notificationType: NotificationType) {
        val state = _uiState.value
        if (state.anime == null) return

        _uiState.update { it.copy(isNotificationTypeSheetVisible = false) }

        viewModelScope.launch {
            toggleAnimeNotificationsUseCase(
                id = state.anime.id,
                notificationType = notificationType
            )
            analyticsTracker.track(AnalyticsEvent.SelectNotificationType(notificationType.name))
            _uiState.update { it.copy(
                snackbarEvent = AnimeDetailSnackbarEvent.NotificationsEnabled(notificationType)
            ) }
        }
    }

    fun notifyPermissionDenied() {
        analyticsTracker.track(AnalyticsEvent.NotificationPermissionDenied)
        _uiState.update { it.copy(snackbarEvent = AnimeDetailSnackbarEvent.NotificationPermissionDenied) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarEvent = null) }
    }

    fun toggleTypeFilter(type: String) {
        _uiState.update { state ->
            val updated = if (type in state.typeFilter) state.typeFilter - type else state.typeFilter + type
            state.copy(typeFilter = updated)
        }
    }

    fun resetTypeFilter() {
        _uiState.update { it.copy(typeFilter = emptySet()) }
    }
}

private data class AnimeCombinedData(
    val anime: Anime?,
    val seasons: List<Season>,
    val titleLanguage: TitleLanguage,
    val isNotificationDebugInfoEnabled: Boolean
)
