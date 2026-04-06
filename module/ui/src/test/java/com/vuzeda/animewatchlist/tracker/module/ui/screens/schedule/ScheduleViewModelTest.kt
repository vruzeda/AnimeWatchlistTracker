package com.vuzeda.animewatchlist.tracker.module.ui.screens.schedule

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveScheduleUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeScheduleUseCase = mockk<ObserveScheduleUseCase>()
    private val observeTitleLanguageUseCase = mockk<ObserveTitleLanguageUseCase>()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ScheduleViewModel(observeScheduleUseCase, observeTitleLanguageUseCase)

    @Test
    fun `defaults to the current calendar season`() = runTest {
        every { observeScheduleUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        val (expectedYear, expectedSeason) = ScheduleViewModel.currentAnimeSeason()

        assertThat(viewModel.uiState.value.selectedYear).isEqualTo(expectedYear)
        assertThat(viewModel.uiState.value.selectedSeason).isEqualTo(expectedSeason)
    }

    @Test
    fun `availableSeasons contains distinct year+season pairs derived from schedule data`() = runTest {
        val seasons = listOf(
            Season(id = 1, malId = 1, title = "Show A", broadcastDay = "Saturdays", isInWatchlist = true, airingSeasonName = "spring", airingSeasonYear = 2026),
            Season(id = 2, malId = 2, title = "Show B", broadcastDay = "Wednesdays", isInWatchlist = true, airingSeasonName = "spring", airingSeasonYear = 2026),
            Season(id = 3, malId = 3, title = "Show C", broadcastDay = "Saturdays", isInWatchlist = true, airingSeasonName = "summer", airingSeasonYear = 2026)
        )
        every { observeScheduleUseCase() } returns flowOf(seasons)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val available = viewModel.uiState.value.availableSeasons

        assertThat(available).containsExactly(
            2026 to AnimeSeason.SPRING,
            2026 to AnimeSeason.SUMMER
        ).inOrder()
    }

    @Test
    fun `availableSeasons excludes seasons with missing airing season metadata`() = runTest {
        val seasons = listOf(
            Season(id = 1, malId = 1, title = "No metadata", broadcastDay = "Saturdays", isInWatchlist = true, airingSeasonName = null, airingSeasonYear = null)
        )
        every { observeScheduleUseCase() } returns flowOf(seasons)
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.availableSeasons).isEmpty()
    }

    @Test
    fun `schedule is filtered to selected season only`() = runTest {
        val (currentYear, currentSeason) = ScheduleViewModel.currentAnimeSeason()
        val seasons = listOf(
            Season(id = 1, malId = 1, title = "Current season show", broadcastDay = "Saturdays", isInWatchlist = true, airingSeasonName = currentSeason.apiValue, airingSeasonYear = currentYear),
            Season(id = 2, malId = 2, title = "Other season show", broadcastDay = "Mondays", isInWatchlist = true, airingSeasonName = "winter", airingSeasonYear = 1999)
        )
        every { observeScheduleUseCase() } returns flowOf(seasons)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val schedule = viewModel.uiState.value.schedule
        val allScheduledIds = schedule.values.flatten().map { it.id }

        assertThat(allScheduledIds).containsExactly(1L)
    }

    @Test
    fun `schedule groups seasons by parsed DayOfWeek`() = runTest {
        val (currentYear, currentSeason) = ScheduleViewModel.currentAnimeSeason()
        val seasons = listOf(
            Season(id = 1, malId = 1, title = "Saturday Show", broadcastDay = "Saturdays", isInWatchlist = true, airingSeasonName = currentSeason.apiValue, airingSeasonYear = currentYear),
            Season(id = 2, malId = 2, title = "Monday Show", broadcastDay = "Mondays", isInWatchlist = true, airingSeasonName = currentSeason.apiValue, airingSeasonYear = currentYear)
        )
        every { observeScheduleUseCase() } returns flowOf(seasons)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val schedule = viewModel.uiState.value.schedule

        assertThat(schedule).containsKey(DayOfWeek.SATURDAY)
        assertThat(schedule).containsKey(DayOfWeek.MONDAY)
        assertThat(schedule[DayOfWeek.SATURDAY]?.map { it.id }).containsExactly(1L)
        assertThat(schedule[DayOfWeek.MONDAY]?.map { it.id }).containsExactly(2L)
    }

    @Test
    fun `schedule is empty when selected season has no shows`() = runTest {
        every { observeScheduleUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.schedule).isEmpty()
    }

    @Test
    fun `onNextSeason advances season and adjusts year on year boundary`() = runTest {
        every { observeScheduleUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        val initialSeason = viewModel.uiState.value.selectedSeason
        val initialYear = viewModel.uiState.value.selectedYear
        val (expectedNext, yearOffset) = initialSeason.next()

        viewModel.onNextSeason()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedSeason).isEqualTo(expectedNext)
        assertThat(viewModel.uiState.value.selectedYear).isEqualTo(initialYear + yearOffset)
    }

    @Test
    fun `onPreviousSeason goes back one season and adjusts year on year boundary`() = runTest {
        every { observeScheduleUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        val initialSeason = viewModel.uiState.value.selectedSeason
        val initialYear = viewModel.uiState.value.selectedYear
        val (expectedPrev, yearOffset) = initialSeason.previous()

        viewModel.onPreviousSeason()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedSeason).isEqualTo(expectedPrev)
        assertThat(viewModel.uiState.value.selectedYear).isEqualTo(initialYear + yearOffset)
    }
}
