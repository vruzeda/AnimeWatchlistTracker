package com.vuzeda.animewatchlist.tracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeListUseCase
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
    private val observeAnimeListUseCase: ObserveAnimeListUseCase
) : ViewModel() {

    private val _filterState = MutableStateFlow(HomeFilterState())
    private val _sortState = MutableStateFlow(HomeSortState())
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeAnimeListUseCase(),
                _filterState,
                _sortState
            ) { animeList, filterState, sortState ->
                HomeUiState(
                    animeList = sortAnimeList(
                        list = applyFilters(animeList, filterState),
                        option = sortState.option,
                        isAscending = sortState.isAscending
                    ),
                    filterState = filterState,
                    sortOption = sortState.option,
                    isSortAscending = sortState.isAscending,
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
    isAscending: Boolean = option.defaultAscending
): List<Anime> {
    val sorted = when (option) {
        HomeSortOption.ALPHABETICAL -> list.sortedBy { it.title.lowercase() }
        HomeSortOption.RECENTLY_ADDED -> list.sortedByDescending { it.addedAt }
        HomeSortOption.USER_RATING -> list.sortedByDescending { it.userRating ?: 0 }
    }
    val shouldReverse = isAscending != option.defaultAscending
    return if (shouldReverse) sorted.reversed() else sorted
}
