package com.vuzeda.animewatchlist.tracker.module.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeDayOfWeek
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveScheduleUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.TriggerAnimeUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val observeScheduleUseCase: ObserveScheduleUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val triggerAnimeUpdateUseCase: TriggerAnimeUpdateUseCase
) : ViewModel() {

    private val _selectedSeason = MutableStateFlow(currentAnimeSeason())
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        observeSchedule()
    }

    private fun observeSchedule() {
        viewModelScope.launch {
            combine(
                observeScheduleUseCase(),
                _selectedSeason,
                observeTitleLanguageUseCase()
            ) { seasons, selected, titleLanguage ->
                val availableSeasons = seasons
                    .mapNotNull { s ->
                        val year = s.airingSeasonYear ?: return@mapNotNull null
                        val name = s.airingSeasonName?.toAnimeSeason() ?: return@mapNotNull null
                        year to name
                    }
                    .distinct()
                    .sortedWith(compareBy({ it.first }, { it.second.ordinal }))

                val filteredSeasons = seasons.filter { season ->
                    season.airingSeasonYear == selected.first &&
                        season.airingSeasonName?.toAnimeSeason() == selected.second
                }

                val schedule = filteredSeasons
                    .groupBy { it.broadcastDay.toDayOfWeek() }
                    .mapValues { (_, seasons) -> seasons.sortedBy { it.broadcastTime } }
                    .toSortedMap()

                ScheduleUiState(
                    selectedYear = selected.first,
                    selectedSeason = selected.second,
                    schedule = schedule,
                    availableSeasons = availableSeasons,
                    titleLanguage = titleLanguage,
                    isLoading = false
                )
            }.catch {
                _uiState.update { it.copy(isLoading = false, hasLoadFailed = true) }
            }.collect { state ->
                _uiState.update { current ->
                    state.copy(snackbarEvent = current.snackbarEvent)
                }
            }
        }
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, hasLoadFailed = false) }
        observeSchedule()
    }

    fun refresh() {
        triggerAnimeUpdateUseCase()
        _uiState.update { it.copy(snackbarEvent = ScheduleSnackbarEvent.UpdateCheckStarted) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarEvent = null) }
    }

    fun onPreviousSeason() {
        _selectedSeason.update { (year, season) ->
            val (prevSeason, yearOffset) = season.previous()
            year + yearOffset to prevSeason
        }
    }

    fun onNextSeason() {
        _selectedSeason.update { (year, season) ->
            val (nextSeason, yearOffset) = season.next()
            year + yearOffset to nextSeason
        }
    }

    companion object {
        fun currentAnimeSeason(): Pair<Int, AnimeSeason> {
            val now = LocalDate.now()
            val season = when (now.monthValue) {
                in 1..3 -> AnimeSeason.WINTER
                in 4..6 -> AnimeSeason.SPRING
                in 7..9 -> AnimeSeason.SUMMER
                else -> AnimeSeason.FALL
            }
            return now.year to season
        }
    }
}

private fun String.toAnimeSeason(): AnimeSeason? {
    val target = this.lowercase()
    return AnimeSeason.entries.firstOrNull { season ->
        val apiValue = when (season) {
            AnimeSeason.WINTER -> "winter"
            AnimeSeason.SPRING -> "spring"
            AnimeSeason.SUMMER -> "summer"
            AnimeSeason.FALL -> "fall"
        }
        apiValue.equals(target, ignoreCase = true)
    }
}

private fun String?.toDayOfWeek(): AnimeDayOfWeek = when (this?.lowercase()?.trimEnd('s')) {
    "monday" -> AnimeDayOfWeek.MONDAY
    "tuesday" -> AnimeDayOfWeek.TUESDAY
    "wednesday" -> AnimeDayOfWeek.WEDNESDAY
    "thursday" -> AnimeDayOfWeek.THURSDAY
    "friday" -> AnimeDayOfWeek.FRIDAY
    "saturday" -> AnimeDayOfWeek.SATURDAY
    "sunday" -> AnimeDayOfWeek.SUNDAY
    else -> AnimeDayOfWeek.UNKNOWN
}
