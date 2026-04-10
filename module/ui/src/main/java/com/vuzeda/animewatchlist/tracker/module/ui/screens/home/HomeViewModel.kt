package com.vuzeda.animewatchlist.tracker.module.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.resolveDisplayTitle
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAllSeasonsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAnimeListUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import java.text.Collator
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeAnimeListUseCase: ObserveAnimeListUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val observeHomeViewModeUseCase: ObserveHomeViewModeUseCase,
    private val observeAllSeasonsUseCase: ObserveAllSeasonsUseCase
) : ViewModel() {

    private val _filterState = MutableStateFlow(HomeFilterState())
    private val _sortState = MutableStateFlow(HomeSortState())
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeAnimeListUseCase(),
                observeAllSeasonsUseCase(),
                _filterState,
                _sortState,
                observeTitleLanguageUseCase(),
                observeHomeViewModeUseCase()
            ) { values ->
                val animeList = values[0] as List<*>
                val seasonList = values[1] as List<*>
                val filterState = values[2] as HomeFilterState
                val sortState = values[3] as HomeSortState
                val titleLanguage = values[4] as com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
                val viewMode = values[5] as HomeViewMode

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
        _filterState.update { it.copy(statusFilter = status) }
    }

    fun selectNotificationFilter(enabled: Boolean?) {
        _filterState.update { it.copy(notificationFilter = enabled) }
    }

    fun resetFilters() {
        _filterState.update { HomeFilterState() }
    }

    fun selectSort(option: HomeSortOption) {
        _sortState.update { current ->
            val isAscending = if (option == current.option) !current.isAscending else option.defaultAscending
            current.copy(option = option, isAscending = isAscending)
        }
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
            animeAddedAt = anime.addedAt
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
    }
    val shouldReverse = isAscending != option.defaultAscending
    return if (shouldReverse) sorted.reversed() else sorted
}

private fun TitleLanguage.toLocale(): Locale = when (this) {
    TitleLanguage.DEFAULT -> Locale.ROOT
    TitleLanguage.ENGLISH -> Locale.ENGLISH
    TitleLanguage.JAPANESE -> Locale("ja")
}
