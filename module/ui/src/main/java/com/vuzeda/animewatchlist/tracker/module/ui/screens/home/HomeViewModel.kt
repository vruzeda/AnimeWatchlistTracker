package com.vuzeda.animewatchlist.tracker.module.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsEvent
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortOption
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortState
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.resolveDisplayTitle
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAllSeasonsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAnimeListUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeNotificationFilterUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeSortStateUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeStatusFilterUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetHomeNotificationFilterUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetHomeSortStateUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetHomeStatusFilterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeAnimeListUseCase: ObserveAnimeListUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val observeHomeViewModeUseCase: ObserveHomeViewModeUseCase,
    private val observeAllSeasonsUseCase: ObserveAllSeasonsUseCase,
    private val observeHomeSortStateUseCase: ObserveHomeSortStateUseCase,
    private val setHomeSortStateUseCase: SetHomeSortStateUseCase,
    private val observeHomeStatusFilterUseCase: ObserveHomeStatusFilterUseCase,
    private val setHomeStatusFilterUseCase: SetHomeStatusFilterUseCase,
    private val observeHomeNotificationFilterUseCase: ObserveHomeNotificationFilterUseCase,
    private val setHomeNotificationFilterUseCase: SetHomeNotificationFilterUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeAnimeListUseCase(),
                observeAllSeasonsUseCase(),
                observeHomeStatusFilterUseCase(),
                observeHomeNotificationFilterUseCase(),
                observeHomeSortStateUseCase(),
                observeTitleLanguageUseCase(),
                observeHomeViewModeUseCase()
            ) { values ->
                val animeList = values[0] as List<*>
                val seasonList = values[1] as List<*>
                val statusFilter = values[2] as WatchStatus?
                val notificationFilter = values[3] as Boolean?
                val sortState = values[4] as HomeSortState
                val titleLanguage = values[5] as TitleLanguage
                val viewMode = values[6] as HomeViewMode

                val filterState = HomeFilterState(statusFilter, notificationFilter)

                @Suppress("UNCHECKED_CAST")
                val typedAnimeList = animeList as List<Anime>
                @Suppress("UNCHECKED_CAST")
                val typedSeasonList = seasonList as List<Season>

                val filteredAnime = sortAnimeList(
                    list = applyFilters(typedAnimeList, filterState),
                    option = sortState.option,
                    isAscending = sortState.isAscending,
                    titleLanguage = titleLanguage
                )

                val seasonItems = if (viewMode == HomeViewMode.SEASON) {
                    buildSeasonItems(
                        animeList = typedAnimeList,
                        seasonList = typedSeasonList,
                        filterState = filterState,
                        sortState = sortState,
                        titleLanguage = titleLanguage
                    )
                } else {
                    emptyList()
                }

                HomeUiState(
                    homeViewMode = viewMode,
                    animeList = filteredAnime,
                    seasonItems = seasonItems,
                    filterState = filterState,
                    sortOption = sortState.option,
                    isSortAscending = sortState.isAscending,
                    titleLanguage = titleLanguage,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun selectStatusFilter(status: WatchStatus?) {
        analyticsTracker.track(AnalyticsEvent.SelectFilter("status", status?.name ?: "reset"))
        viewModelScope.launch { setHomeStatusFilterUseCase(status) }
    }

    fun selectNotificationFilter(enabled: Boolean?) {
        analyticsTracker.track(AnalyticsEvent.SelectFilter("notification", enabled?.toString() ?: "reset"))
        viewModelScope.launch { setHomeNotificationFilterUseCase(enabled) }
    }

    fun resetFilters() {
        analyticsTracker.track(AnalyticsEvent.SelectFilter("all", "reset"))
        viewModelScope.launch {
            setHomeStatusFilterUseCase(null)
            setHomeNotificationFilterUseCase(null)
        }
    }

    fun selectSort(option: HomeSortOption) {
        val current = _uiState.value
        val isAscending = if (option == current.sortOption) !current.isSortAscending else option.defaultAscending
        analyticsTracker.track(AnalyticsEvent.SelectSort("home", option.name, isAscending))
        viewModelScope.launch { setHomeSortStateUseCase(HomeSortState(option, isAscending)) }
    }
}

fun buildSeasonItems(
    animeList: List<Anime>,
    seasonList: List<Season>,
    filterState: HomeFilterState,
    sortState: HomeSortState,
    titleLanguage: TitleLanguage = TitleLanguage.DEFAULT
): List<HomeSeasonItem> {
    val animeMap = animeList.associateBy { it.id }

    val enriched = seasonList.mapNotNull { season ->
        if (!season.isInWatchlist) return@mapNotNull null
        val anime = animeMap[season.animeId] ?: return@mapNotNull null
        HomeSeasonItem(
            season = season,
            animeStatus = anime.status,
            animeNotificationType = anime.notificationType,
            animeAddedAt = anime.addedAt,
            animeImageUrl = anime.imageUrl
        )
    }

    val filtered = applySeasonFilters(enriched, filterState)
    return sortSeasonItems(filtered, sortState.option, sortState.isAscending, titleLanguage)
}

fun applySeasonFilters(
    items: List<HomeSeasonItem>,
    filterState: HomeFilterState
): List<HomeSeasonItem> {
    var filtered = items
    filterState.statusFilter?.let { status ->
        filtered = filtered.filter { it.animeStatus == status }
    }
    filterState.notificationFilter?.let { enabled ->
        filtered = filtered.filter {
            (it.animeNotificationType != NotificationType.NONE) == enabled
        }
    }
    return filtered
}

fun sortSeasonItems(
    items: List<HomeSeasonItem>,
    option: HomeSortOption,
    isAscending: Boolean = option.defaultAscending,
    titleLanguage: TitleLanguage = TitleLanguage.DEFAULT
): List<HomeSeasonItem> {
    val sorted = when (option) {
        HomeSortOption.ALPHABETICAL -> {
            val collator = Collator.getInstance(titleLanguage.toLocale())
            items.sortedWith(compareBy(collator) {
                resolveDisplayTitle(it.season.title, it.season.titleEnglish, it.season.titleJapanese, titleLanguage)
            })
        }
        HomeSortOption.RECENTLY_ADDED -> items.sortedByDescending { it.animeAddedAt }
        HomeSortOption.USER_RATING -> items.sortedByDescending { it.season.score ?: 0.0 }
        HomeSortOption.WATCH_STATUS -> items.sortedBy { it.animeStatus.ordinal }
    }
    val shouldReverse = isAscending != option.defaultAscending
    return if (shouldReverse) sorted.reversed() else sorted
}

fun applyFilters(list: List<Anime>, filterState: HomeFilterState): List<Anime> {
    var filtered = list
    filterState.statusFilter?.let { status ->
        filtered = filtered.filter { it.status == status }
    }
    filterState.notificationFilter?.let { enabled ->
        filtered = filtered.filter { it.isNotificationsEnabled == enabled }
    }
    return filtered
}

fun sortAnimeList(
    list: List<Anime>,
    option: HomeSortOption,
    isAscending: Boolean = option.defaultAscending,
    titleLanguage: TitleLanguage = TitleLanguage.DEFAULT
): List<Anime> {
    val sorted = when (option) {
        HomeSortOption.ALPHABETICAL -> {
            val collator = Collator.getInstance(titleLanguage.toLocale())
            list.sortedWith(compareBy(collator) {
                resolveDisplayTitle(it.title, it.titleEnglish, it.titleJapanese, titleLanguage)
            })
        }
        HomeSortOption.RECENTLY_ADDED -> list.sortedByDescending { it.addedAt }
        HomeSortOption.USER_RATING -> list.sortedByDescending { it.userRating ?: 0 }
        HomeSortOption.WATCH_STATUS -> list.sortedBy { it.status.ordinal }
    }
    val shouldReverse = isAscending != option.defaultAscending
    return if (shouldReverse) sorted.reversed() else sorted
}

private fun TitleLanguage.toLocale(): Locale = when (this) {
    TitleLanguage.DEFAULT -> Locale.ROOT
    TitleLanguage.ENGLISH -> Locale.ENGLISH
    TitleLanguage.JAPANESE -> Locale.JAPANESE
}
