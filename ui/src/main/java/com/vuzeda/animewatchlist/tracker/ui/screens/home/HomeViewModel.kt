package com.vuzeda.animewatchlist.tracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeByNotificationUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeAnimeListUseCase: ObserveAnimeListUseCase,
    private val observeAnimeByNotificationUseCase: ObserveAnimeByNotificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeWatchlist(HomeFilter.All)
    }

    fun selectFilter(filter: HomeFilter) {
        _uiState.update { it.copy(selectedFilter = filter, isLoading = true) }
        observeWatchlist(filter)
    }

    fun selectSort(option: HomeSortOption) {
        _uiState.update { currentState ->
            val isAscending = if (option == currentState.sortOption) {
                !currentState.isSortAscending
            } else {
                option.defaultAscending
            }
            currentState.copy(
                sortOption = option,
                isSortAscending = isAscending,
                animeList = sortAnimeList(currentState.animeList, option, isAscending)
            )
        }
    }

    private fun observeWatchlist(filter: HomeFilter) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val flow = when (filter) {
                is HomeFilter.All -> observeAnimeListUseCase()
                is HomeFilter.ByStatus -> observeAnimeListUseCase(filter.status)
                is HomeFilter.NotificationsOn -> observeAnimeByNotificationUseCase(enabled = true)
                is HomeFilter.NotificationsOff -> observeAnimeByNotificationUseCase(enabled = false)
            }
            flow.collect { animeList ->
                _uiState.update {
                    it.copy(
                        animeList = sortAnimeList(animeList, it.sortOption, it.isSortAscending),
                        isLoading = false
                    )
                }
            }
        }
    }
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
