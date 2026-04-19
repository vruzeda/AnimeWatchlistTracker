package com.vuzeda.animewatchlist.tracker.module.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveScheduleUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val observeScheduleUseCase: ObserveScheduleUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase
) : ViewModel() {

    private val _selectedSeason = MutableStateFlow(currentAnimeSeason())
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
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
                    .groupBy { it.broadcastDay?.toDayOfWeek() ?: DayOfWeek.MONDAY }
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
            }.collect { state ->
                _uiState.value = state
            }
        }
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

private fun String.toAnimeSeason(): AnimeSeason? =
    AnimeSeason.entries.firstOrNull { it.apiValue.equals(this, ignoreCase = true) }

private fun String.toDayOfWeek(): DayOfWeek = when (this.lowercase().trimEnd('s')) {
    "monday" -> DayOfWeek.MONDAY
    "tuesday" -> DayOfWeek.TUESDAY
    "wednesday" -> DayOfWeek.WEDNESDAY
    "thursday" -> DayOfWeek.THURSDAY
    "friday" -> DayOfWeek.FRIDAY
    "saturday" -> DayOfWeek.SATURDAY
    "sunday" -> DayOfWeek.SUNDAY
    else -> DayOfWeek.MONDAY
}
