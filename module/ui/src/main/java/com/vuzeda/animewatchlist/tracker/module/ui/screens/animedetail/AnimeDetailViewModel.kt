package com.vuzeda.animewatchlist.tracker.module.ui.screens.animedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
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
    private val observeIsNotificationDebugInfoEnabledUseCase: ObserveIsNotificationDebugInfoEnabledUseCase
) : ViewModel() {

    private val animeId: Long = checkNotNull(savedStateHandle["animeId"])
    private val malId: Int = savedStateHandle["malId"] ?: 0

    private val _uiState = MutableStateFlow<AnimeDetailUiState>(AnimeDetailUiState.Loading)
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    private var resolvedAnime: Anime? = null
    private var resolvedSeasons: List<Season> = emptyList()
    private var latestIsNotificationDebugInfoEnabled: Boolean = false

    init {
        observeTitleLanguage()
        observeNotificationDebugInfo()
        if (animeId > 0) {
            observeAnime(animeId)
        } else if (malId > 0) {
            resolveFromApi()
        } else {
            _uiState.value = AnimeDetailUiState.NotFound
        }
    }

    private fun observeTitleLanguage() {
        viewModelScope.launch {
            observeTitleLanguageUseCase().collect { titleLanguage ->
                _uiState.update { state ->
                    when (state) {
                        is AnimeDetailUiState.Success -> state.copy(titleLanguage = titleLanguage)
                        else -> state
                    }
                }
            }
        }
    }

    private fun observeNotificationDebugInfo() {
        viewModelScope.launch {
            observeIsNotificationDebugInfoEnabledUseCase().collect { enabled ->
                latestIsNotificationDebugInfoEnabled = enabled
                _uiState.update { state ->
                    when (state) {
                        is AnimeDetailUiState.Success -> state.copy(isNotificationDebugInfoEnabled = enabled)
                        else -> state
                    }
                }
            }
        }
    }

    private fun observeAnime(id: Long) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                observeAnimeByIdUseCase(id),
                observeSeasonsForAnimeUseCase(id)
            ) { anime, seasons ->
                anime to seasons
            }.collect { (anime, seasons) ->
                if (anime != null) {
                    _uiState.update { currentState ->
                        when (currentState) {
                            is AnimeDetailUiState.Success -> currentState.copy(
                                anime = anime,
                                seasons = seasons,
                                isInWatchlist = true,
                                notificationType = anime.notificationType
                            )
                            else -> AnimeDetailUiState.Success(
                                anime = anime,
                                seasons = seasons,
                                isInWatchlist = true,
                                isNotificationDebugInfoEnabled = latestIsNotificationDebugInfoEnabled
                            )
                        }
                    }
                } else {
                    _uiState.update { current ->
                        if (current is AnimeDetailUiState.Success && !current.isInWatchlist) current
                        else AnimeDetailUiState.NotFound
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
                    _uiState.update { if (it is AnimeDetailUiState.Success) it.copy(isRefreshing = false) else it }
                }
                observeAnime(existingAnimeId)
                return@launch
            }

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
                    resolvedAnime = anime
                    resolvedSeasons = seasons
                    if (isRefresh) {
                        _uiState.update { state ->
                            if (state is AnimeDetailUiState.Success) state.copy(
                                anime = anime,
                                seasons = seasons,
                                isRefreshing = false
                            ) else AnimeDetailUiState.Success(anime = anime, seasons = seasons, isInWatchlist = false, isNotificationDebugInfoEnabled = latestIsNotificationDebugInfoEnabled)
                        }
                    } else {
                        _uiState.value = AnimeDetailUiState.Success(
                            anime = anime,
                            seasons = seasons,
                            isInWatchlist = false,
                            isNotificationDebugInfoEnabled = latestIsNotificationDebugInfoEnabled
                        )
                    }
                }
                .onFailure {
                    if (isRefresh) {
                        _uiState.update { if (it is AnimeDetailUiState.Success) it.copy(isRefreshing = false) else it }
                    } else {
                        _uiState.value = AnimeDetailUiState.NotFound
                    }
                }
        }
    }

    fun refresh() {
        if (_uiState.value !is AnimeDetailUiState.Success) return
        _uiState.update { if (it is AnimeDetailUiState.Success) it.copy(isRefreshing = true) else it }
        if (animeId > 0) {
            viewModelScope.launch {
                runCatching { refreshAnimeSeasonsUseCase(animeId) }
                val seasons = observeSeasonsForAnimeUseCase(animeId).first()
                seasons.forEach { season -> runCatching { refreshSeasonDataUseCase(season) } }
                _uiState.update { if (it is AnimeDetailUiState.Success) it.copy(isRefreshing = false) else it }
            }
        } else if (malId > 0) {
            resolveFromApi(isRefresh = true)
        }
    }

    fun showStatusSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isStatusSheetVisible = true)
            else state
        }
    }

    fun dismissStatusSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isStatusSheetVisible = false)
            else state
        }
    }

    fun showAddSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isAddSheetVisible = true)
            else state
        }
    }

    fun dismissAddSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isAddSheetVisible = false)
            else state
        }
    }

    fun showAddScopeSheet(status: WatchStatus) {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(
                isAddSheetVisible = false,
                isAddScopeSheetVisible = true,
                pendingAddStatus = status
            ) else state
        }
    }

    fun dismissAddScopeSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(
                isAddScopeSheetVisible = false,
                pendingAddStatus = null
            ) else state
        }
    }

    fun showAddSeasonSheet(season: Season) {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(
                isAddSeasonSheetVisible = true,
                pendingAddSeason = season
            ) else state
        }
    }

    fun dismissAddSeasonSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(
                isAddSeasonSheetVisible = false,
                pendingAddSeason = null
            ) else state
        }
    }

    fun confirmAddSeason(status: WatchStatus) {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return
        val season = state.pendingAddSeason ?: return

        _uiState.update {
            if (it is AnimeDetailUiState.Success) it.copy(
                isAddSeasonSheetVisible = false,
                pendingAddSeason = null
            ) else it
        }

        viewModelScope.launch {
            if (season.id > 0) {
                addSeasonToWatchlistUseCase(season, status)
            } else {
                val anime = resolvedAnime ?: return@launch
                val newId = addAnimeUseCase(anime, listOf(season), status)
                resolvedAnime = null
                resolvedSeasons = emptyList()
                _uiState.update {
                    if (it is AnimeDetailUiState.Success) it.copy(
                        snackbarEvent = AnimeDetailSnackbarEvent.AddedToWatchlist(anime.title)
                    ) else it
                }
                observeAnime(newId)
            }
        }
    }

    fun confirmAddScope(allSeasons: Boolean) {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return
        val status = state.pendingAddStatus ?: return
        val anime = resolvedAnime ?: return
        val seasons = if (allSeasons) resolvedSeasons else resolvedSeasons.take(1)
        val title = anime.title

        _uiState.update {
            if (it is AnimeDetailUiState.Success) it.copy(
                isAddScopeSheetVisible = false,
                pendingAddStatus = null
            ) else it
        }

        viewModelScope.launch {
            val newId = addAnimeUseCase(
                anime = anime,
                seasons = seasons,
                status = status
            )
            resolvedAnime = null
            resolvedSeasons = emptyList()
            _uiState.update {
                if (it is AnimeDetailUiState.Success) it.copy(
                    snackbarEvent = AnimeDetailSnackbarEvent.AddedToWatchlist(title)
                ) else it
            }
            observeAnime(newId)
        }
    }

    fun updateStatus(status: WatchStatus) {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return
        val mostRecentSeason = state.seasons.maxByOrNull { it.orderIndex } ?: return

        viewModelScope.launch {
            updateSeasonStatusUseCase(mostRecentSeason, status)
        }
    }

    fun updateUserRating(rating: Int) {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return

        viewModelScope.launch {
            updateAnimeUseCase(state.anime.copy(userRating = if (rating > 0) rating else null))
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isDeleteConfirmationVisible = true)
            else state
        }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isDeleteConfirmationVisible = false)
            else state
        }
    }

    fun confirmDelete() {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return

        viewModelScope.launch {
            deleteAnimeUseCase(state.anime.id)
            _uiState.update { current ->
                if (current is AnimeDetailUiState.Success) current.copy(
                    isInWatchlist = false,
                    isDeleteConfirmationVisible = false
                ) else current
            }
        }
    }

    fun onNotificationIconClick() {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return

        if (state.isNotificationsEnabled) {
            viewModelScope.launch {
                toggleAnimeNotificationsUseCase(
                    id = state.anime.id,
                    notificationType = NotificationType.NONE
                )
                _uiState.update {
                    if (it is AnimeDetailUiState.Success) it.copy(
                        snackbarEvent = AnimeDetailSnackbarEvent.NotificationsDisabled
                    ) else it
                }
            }
        } else {
            _uiState.update {
                if (it is AnimeDetailUiState.Success) it.copy(isNotificationTypeSheetVisible = true)
                else it
            }
        }
    }

    fun dismissNotificationTypeSheet() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(isNotificationTypeSheetVisible = false)
            else state
        }
    }

    fun selectNotificationType(notificationType: NotificationType) {
        val state = _uiState.value
        if (state !is AnimeDetailUiState.Success) return

        _uiState.update {
            if (it is AnimeDetailUiState.Success) it.copy(isNotificationTypeSheetVisible = false)
            else it
        }

        viewModelScope.launch {
            toggleAnimeNotificationsUseCase(
                id = state.anime.id,
                notificationType = notificationType
            )
            _uiState.update {
                if (it is AnimeDetailUiState.Success) it.copy(
                    snackbarEvent = AnimeDetailSnackbarEvent.NotificationsEnabled(notificationType)
                ) else it
            }
        }
    }

    fun notifyPermissionDenied() {
        _uiState.update {
            if (it is AnimeDetailUiState.Success) it.copy(snackbarEvent = AnimeDetailSnackbarEvent.NotificationPermissionDenied)
            else it
        }
    }

    fun clearSnackbar() {
        _uiState.update { state ->
            if (state is AnimeDetailUiState.Success) state.copy(snackbarEvent = null)
            else state
        }
    }
}
