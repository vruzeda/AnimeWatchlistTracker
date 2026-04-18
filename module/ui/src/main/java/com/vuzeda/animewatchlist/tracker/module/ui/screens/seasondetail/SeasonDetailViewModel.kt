package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsEvent
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeFromDetailsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.AddSeasonToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteSeasonUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FillEpisodeGapsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FindSeasonIdByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveIsNotificationDebugInfoEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonByIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveWatchedEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RefreshSeasonDataUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetAllEpisodesWatchedUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetEpisodeWatchedUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ToggleSeasonEpisodeNotificationsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.UpdateSeasonStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
open class SeasonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeSeasonByIdUseCase: ObserveSeasonByIdUseCase,
    private val observeSeasonsForAnimeUseCase: ObserveSeasonsForAnimeUseCase,
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase,
    private val fetchEpisodesUseCase: FetchEpisodesUseCase,
    private val fillEpisodeGapsUseCase: FillEpisodeGapsUseCase,
    private val updateSeasonStatusUseCase: UpdateSeasonStatusUseCase,
    private val deleteSeasonUseCase: DeleteSeasonUseCase,
    private val addSeasonToWatchlistUseCase: AddSeasonToWatchlistUseCase,
    private val addAnimeFromDetailsUseCase: AddAnimeFromDetailsUseCase,
    private val findSeasonIdByMalIdUseCase: FindSeasonIdByMalIdUseCase,
    private val toggleSeasonEpisodeNotificationsUseCase: ToggleSeasonEpisodeNotificationsUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val refreshSeasonDataUseCase: RefreshSeasonDataUseCase,
    private val observeIsNotificationDebugInfoEnabledUseCase: ObserveIsNotificationDebugInfoEnabledUseCase,
    private val observeWatchedEpisodesUseCase: ObserveWatchedEpisodesUseCase,
    private val setEpisodeWatchedUseCase: SetEpisodeWatchedUseCase,
    private val setAllEpisodesWatchedUseCase: SetAllEpisodesWatchedUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val seasonId: Long = checkNotNull(savedStateHandle["seasonId"])
    private val malId: Int = savedStateHandle["malId"] ?: 0

    private var pendingDetails: AnimeFullDetails? = null
    private var observingSiblingsForAnimeId: Long = -1L

    private val _uiState = MutableStateFlow(SeasonDetailUiState())
    val uiState: StateFlow<SeasonDetailUiState> = _uiState.asStateFlow()

    init {
        if (seasonId > 0) {
            observeSeasonData()
        } else if (malId > 0) {
            loadFromApi()
        } else {
            _uiState.update { it.copy(isLoading = false, isNotFound = true) }
        }
    }

    private fun observeSiblingCount(animeId: Long) {
        if (observingSiblingsForAnimeId == animeId) return
        observingSiblingsForAnimeId = animeId
        viewModelScope.launch {
            observeSeasonsForAnimeUseCase(animeId).collect { siblings ->
                _uiState.update { it.copy(isLastSeason = siblings.count { s -> s.isInWatchlist } <= 1) }
            }
        }
    }

    private fun observeSeasonData() {
        viewModelScope.launch {
            combine(
                observeSeasonByIdUseCase(seasonId),
                observeWatchedEpisodesUseCase(seasonId),
                observeTitleLanguageUseCase(),
                observeIsNotificationDebugInfoEnabledUseCase()
            ) { season, watchedEpisodes, titleLanguage, isNotificationDebugInfoEnabled ->
                SeasonCombinedData(season, watchedEpisodes, titleLanguage, isNotificationDebugInfoEnabled)
            }.collect { (season, watchedEpisodes, titleLanguage, isNotificationDebugInfoEnabled) ->
                if (season != null) observeSiblingCount(season.animeId)
                _uiState.update { currentState ->
                    if (season == null) {
                        if (currentState.season != null && !currentState.isInWatchlist) currentState
                        else currentState.copy(isLoading = false, isNotFound = true, season = null)
                    } else if (currentState.season == null) {
                        loadEpisodes(season.malId, page = 1)
                        currentState.copy(
                            isLoading = false,
                            isNotFound = false,
                            season = season,
                            isInWatchlist = season.isInWatchlist,
                            isEpisodeNotificationsEnabled = season.isEpisodeNotificationsEnabled,
                            watchedEpisodes = watchedEpisodes,
                            titleLanguage = titleLanguage,
                            isNotificationDebugInfoEnabled = isNotificationDebugInfoEnabled,
                            isLoadingEpisodes = true,
                            broadcastLocalTime = computeBroadcastLocalTime(season)
                        )
                    } else {
                        currentState.copy(
                            season = season,
                            isInWatchlist = season.isInWatchlist,
                            watchedEpisodes = watchedEpisodes,
                            titleLanguage = titleLanguage,
                            isNotificationDebugInfoEnabled = isNotificationDebugInfoEnabled
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            val season = observeSeasonByIdUseCase(seasonId).first() ?: return@launch
            runCatching { refreshSeasonDataUseCase(season) }
        }
    }

    fun setEpisodeWatched(episodeNumber: Int, isWatched: Boolean) {
        val state = _uiState.value
        if (state.season == null || !state.isInWatchlist) return
        viewModelScope.launch {
            setEpisodeWatchedUseCase(state.season.id, episodeNumber, isWatched)
            analyticsTracker.track(AnalyticsEvent.SetEpisodeWatched(isWatched))
        }
    }

    fun markAllEpisodesWatched() {
        val state = _uiState.value
        if (state.season == null || !state.isInWatchlist) return
        val episodeNumbers = state.episodes.map { it.number }
        if (episodeNumbers.isEmpty()) return
        viewModelScope.launch {
            setAllEpisodesWatchedUseCase(state.season.id, episodeNumbers)
            analyticsTracker.track(AnalyticsEvent.MarkAllEpisodesWatched)
        }
    }

    private fun loadFromApi(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val existingSeasonId = findSeasonIdByMalIdUseCase(malId)
            if (existingSeasonId != null) {
                if (isRefresh) {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                observeSeason(existingSeasonId)
                return@launch
            }

            val titleLanguage = observeTitleLanguageUseCase().first()
            val isNotificationDebugInfoEnabled = observeIsNotificationDebugInfoEnabledUseCase().first()

            fetchSeasonDetailUseCase(malId)
                .onSuccess { details ->
                    pendingDetails = details
                    val season = Season(
                        malId = details.malId,
                        title = details.title,
                        titleEnglish = details.titleEnglish,
                        titleJapanese = details.titleJapanese,
                        imageUrl = details.imageUrl,
                        type = details.type,
                        episodeCount = details.episodes,
                        score = details.score,
                        airingStatus = details.airingStatus,
                        broadcastInfo = details.broadcastInfo,
                        broadcastDay = details.broadcastDay,
                        broadcastTime = details.broadcastTime,
                        broadcastTimezone = details.broadcastTimezone,
                        streamingLinks = details.streamingLinks
                    )
                    loadEpisodes(details.malId, page = 1)
                    if (isRefresh) {
                        _uiState.update { it.copy(
                            season = season,
                            isRefreshing = false,
                            broadcastLocalTime = computeBroadcastLocalTime(season)
                        ) }
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false,
                            isNotFound = false,
                            season = season,
                            isInWatchlist = false,
                            isLoadingEpisodes = true,
                            titleLanguage = titleLanguage,
                            broadcastLocalTime = computeBroadcastLocalTime(season),
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
        if (_uiState.value.season == null) return
        _uiState.update { it.copy(isRefreshing = true) }
        if (seasonId > 0) {
            viewModelScope.launch {
                val season = observeSeasonByIdUseCase(seasonId).first()
                if (season != null) runCatching { refreshSeasonDataUseCase(season) }
                _uiState.update { it.copy(isRefreshing = false) }
            }
        } else if (malId > 0) {
            loadFromApi(isRefresh = true)
        }
    }

    private fun loadEpisodes(malId: Int, page: Int) {
        viewModelScope.launch {
            fetchEpisodesUseCase(malId = malId, page = page)
                .onSuccess { episodePage ->
                    _uiState.update { state ->
                        val accumulated = state.episodes + episodePage.episodes
                        val resolved = if (!episodePage.hasNextPage)
                            fillEpisodeGapsUseCase(accumulated, state.season?.episodeCount)
                        else
                            accumulated
                        state.copy(
                            episodes = resolved,
                            isLoadingEpisodes = false,
                            hasMoreEpisodes = episodePage.hasNextPage,
                            nextEpisodePage = episodePage.nextPage
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingEpisodes = false) }
                }
        }
    }

    fun loadMoreEpisodes() {
        val state = _uiState.value
        if (state.season == null || state.isLoadingEpisodes || !state.hasMoreEpisodes) return

        _uiState.update { it.copy(isLoadingEpisodes = true) }
        loadEpisodes(malId = state.season.malId, page = state.nextEpisodePage)
    }

    fun showStatusSheet() {
        _uiState.update { it.copy(isStatusSheetVisible = true) }
    }

    fun dismissStatusSheet() {
        _uiState.update { it.copy(isStatusSheetVisible = false) }
    }

    fun updateStatus(status: WatchStatus) {
        val season = _uiState.value.season ?: return

        viewModelScope.launch {
            updateSeasonStatusUseCase(season, status)
            analyticsTracker.track(AnalyticsEvent.UpdateSeasonStatus(status.name))
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(isDeleteConfirmationVisible = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(isDeleteConfirmationVisible = false) }
    }

    fun confirmDelete() {
        val state = _uiState.value
        val season = state.season ?: return

        viewModelScope.launch {
            deleteSeasonUseCase(season)
            analyticsTracker.track(AnalyticsEvent.RemoveSeason(state.isLastSeason))
            _uiState.update { it.copy(
                isInWatchlist = false,
                isDeleteConfirmationVisible = false
            ) }
        }
    }

    fun showAddSheet() {
        _uiState.update { it.copy(isAddSheetVisible = true) }
    }

    fun dismissAddSheet() {
        _uiState.update { it.copy(isAddSheetVisible = false) }
    }

    fun addToWatchlist(status: WatchStatus) {
        val state = _uiState.value
        val season = state.season ?: return

        if (season.id > 0) {
            viewModelScope.launch {
                addSeasonToWatchlistUseCase(season, status)
                analyticsTracker.track(AnalyticsEvent.AddSeason(status.name))
                _uiState.update { it.copy(
                    isAddSheetVisible = false,
                    snackbarEvent = SeasonDetailSnackbarEvent.AddedToWatchlist(season.title)
                ) }
            }
            return
        }

        val details = pendingDetails ?: return
        viewModelScope.launch {
            addAnimeFromDetailsUseCase(details, status)
            analyticsTracker.track(AnalyticsEvent.AddSeason(status.name))
            pendingDetails = null
            _uiState.update { it.copy(
                isAddSheetVisible = false,
                snackbarEvent = SeasonDetailSnackbarEvent.AddedToWatchlist(details.title)
            ) }
            val addedSeasonId = findSeasonIdByMalIdUseCase(malId)
            if (addedSeasonId != null) {
                observeSeason(addedSeasonId)
            }
        }
    }

    fun toggleEpisodeNotifications() {
        val state = _uiState.value
        if (state.season == null) return

        val newEnabled = !state.isEpisodeNotificationsEnabled
        viewModelScope.launch {
            toggleSeasonEpisodeNotificationsUseCase(
                seasonId = state.season.id,
                enabled = newEnabled
            )
            analyticsTracker.track(AnalyticsEvent.ToggleEpisodeNotifications(newEnabled))
            _uiState.update { it.copy(
                isEpisodeNotificationsEnabled = newEnabled,
                snackbarEvent = SeasonDetailSnackbarEvent.EpisodeNotificationsToggled(newEnabled)
            ) }
        }
    }

    fun navigateToAnimeDetail() {
        val season = _uiState.value.season ?: return
        _uiState.update { it.copy(pendingNavigationMalId = season.malId) }
    }

    fun onNavigated() {
        _uiState.update { it.copy(pendingNavigationMalId = null) }
    }

    fun notifyPermissionDenied() {
        analyticsTracker.track(AnalyticsEvent.NotificationPermissionDenied)
        _uiState.update { it.copy(snackbarEvent = SeasonDetailSnackbarEvent.NotificationPermissionDenied) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarEvent = null) }
    }

    protected open fun localZoneId(): ZoneId = ZoneId.systemDefault()

    private fun computeBroadcastLocalTime(season: Season): LocalBroadcastTime? {
        val day = season.broadcastDay ?: return null
        val time = season.broadcastTime ?: return null
        val timezone = season.broadcastTimezone ?: return null
        return try {
            val sourceZone = ZoneId.of(timezone)
            val localZone = localZoneId()
            val broadcastTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            val dayOfWeek = DayOfWeek.entries.firstOrNull { dow ->
                day.lowercase().startsWith(dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase())
            } ?: return null
            val reference = ZonedDateTime.now(sourceZone)
                .with(dayOfWeek)
                .withHour(broadcastTime.hour)
                .withMinute(broadcastTime.minute)
                .withSecond(0)
                .withNano(0)
            val local = reference.withZoneSameInstant(localZone)
            LocalBroadcastTime(
                day = local.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                time = local.format(DateTimeFormatter.ofPattern("HH:mm")),
                zone = localZone.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun observeSeason(seasonId: Long) {
        viewModelScope.launch {
            combine(
                observeSeasonByIdUseCase(seasonId),
                observeWatchedEpisodesUseCase(seasonId),
                observeTitleLanguageUseCase(),
                observeIsNotificationDebugInfoEnabledUseCase()
            ) { season, watchedEpisodes, titleLanguage, isNotificationDebugInfoEnabled ->
                SeasonCombinedData(season, watchedEpisodes, titleLanguage, isNotificationDebugInfoEnabled)
            }.collect { (season, watchedEpisodes, titleLanguage, isNotificationDebugInfoEnabled) ->
                if (season != null) observeSiblingCount(season.animeId)
                _uiState.update { currentState ->
                    if (season == null) {
                        currentState.copy(isLoading = false, isNotFound = true, season = null)
                    } else if (currentState.season == null) {
                        loadEpisodes(season.malId, page = 1)
                        currentState.copy(
                            isLoading = false,
                            isNotFound = false,
                            season = season,
                            isInWatchlist = true,
                            isEpisodeNotificationsEnabled = season.isEpisodeNotificationsEnabled,
                            watchedEpisodes = watchedEpisodes,
                            titleLanguage = titleLanguage,
                            isNotificationDebugInfoEnabled = isNotificationDebugInfoEnabled,
                            isLoadingEpisodes = true,
                            broadcastLocalTime = computeBroadcastLocalTime(season)
                        )
                    } else {
                        currentState.copy(
                            season = season,
                            isInWatchlist = true,
                            isEpisodeNotificationsEnabled = season.isEpisodeNotificationsEnabled,
                            watchedEpisodes = watchedEpisodes,
                            titleLanguage = titleLanguage,
                            isNotificationDebugInfoEnabled = isNotificationDebugInfoEnabled
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            val season = observeSeasonByIdUseCase(seasonId).first() ?: return@launch
            runCatching { refreshSeasonDataUseCase(season) }
        }
    }
}

private data class SeasonCombinedData(
    val season: Season?,
    val watchedEpisodes: Set<Int>,
    val titleLanguage: TitleLanguage,
    val isNotificationDebugInfoEnabled: Boolean
)
