package com.vuzeda.animewatchlist.tracker.module.ui.screens.schedule

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeDayOfWeek
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.BroadcastTime
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveScheduleUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.TriggerAnimeUpdateUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeScheduleUseCase = mockk<ObserveScheduleUseCase>()
    private val observeTitleLanguageUseCase = mockk<ObserveTitleLanguageUseCase>()
    private val triggerAnimeUpdateUseCase = mockk<TriggerAnimeUpdateUseCase>(relaxed = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        ScheduleViewModel(observeScheduleUseCase, observeTitleLanguageUseCase, triggerAnimeUpdateUseCase)

    @Test
    fun `schedule flow failure surfaces the load failed state`() = runTest {
        every { observeScheduleUseCase() } returns flow { throw IllegalStateException("db error") }
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasLoadFailed).isTrue()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `retry after failure resubscribes and recovers`() = runTest {
        every { observeScheduleUseCase() } returns flow { throw IllegalStateException("db error") }
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.hasLoadFailed).isTrue()

        every { observeScheduleUseCase() } returns flowOf(emptyList())
        viewModel.retry()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasLoadFailed).isFalse()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `refresh triggers an immediate update check and emits snackbar event`() = runTest {
        every { observeScheduleUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.refresh()

        verify(exactly = 1) { triggerAnimeUpdateUseCase() }
        assertThat(viewModel.uiState.value.snackbarEvent).isEqualTo(ScheduleSnackbarEvent.UpdateCheckStarted)
    }

    @Test
    fun `clearSnackbar removes the pending snackbar event`() = runTest {
        every { observeScheduleUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()

        viewModel.clearSnackbar()

        assertThat(viewModel.uiState.value.snackbarEvent).isNull()
    }

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
            Season(id = 1, malId = 1, title = "Show A", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.SATURDAY), isInWatchlist = true, airingSeasonName = "spring", airingSeasonYear = 2026),
            Season(id = 2, malId = 2, title = "Show B", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.WEDNESDAY), isInWatchlist = true, airingSeasonName = "spring", airingSeasonYear = 2026),
            Season(id = 3, malId = 3, title = "Show C", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.SATURDAY), isInWatchlist = true, airingSeasonName = "summer", airingSeasonYear = 2026)
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
            Season(id = 1, malId = 1, title = "No metadata", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.SATURDAY), isInWatchlist = true, airingSeasonName = null, airingSeasonYear = null)
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
            Season(id = 1, malId = 1, title = "Current season show", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.SATURDAY), isInWatchlist = true, airingSeasonName = currentSeason.name.lowercase(), airingSeasonYear = currentYear),
            Season(id = 2, malId = 2, title = "Other season show", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.MONDAY), isInWatchlist = true, airingSeasonName = "winter", airingSeasonYear = 1999)
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
            Season(id = 1, malId = 1, title = "Saturday Show", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.SATURDAY), isInWatchlist = true, airingSeasonName = currentSeason.name.lowercase(), airingSeasonYear = currentYear),
            Season(id = 2, malId = 2, title = "Monday Show", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.MONDAY), isInWatchlist = true, airingSeasonName = currentSeason.name.lowercase(), airingSeasonYear = currentYear),
            Season(id = 3, malId = 3, title = "Unknown Day Show", broadcastTime = null, isInWatchlist = true, airingSeasonName = currentSeason.name.lowercase(), airingSeasonYear = currentYear),
        )
        every { observeScheduleUseCase() } returns flowOf(seasons)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val schedule = viewModel.uiState.value.schedule

        assertThat(schedule).containsKey(AnimeDayOfWeek.SATURDAY)
        assertThat(schedule).containsKey(AnimeDayOfWeek.MONDAY)
        assertThat(schedule).containsKey(AnimeDayOfWeek.UNKNOWN)
        assertThat(schedule[AnimeDayOfWeek.SATURDAY]?.map { it.id }).containsExactly(1L)
        assertThat(schedule[AnimeDayOfWeek.MONDAY]?.map { it.id }).containsExactly(2L)
        assertThat(schedule[AnimeDayOfWeek.UNKNOWN]?.map { it.id }).containsExactly(3L)
    }

    @Test
    fun `same-day entries are ordered by broadcast time`() = runTest {
        val (currentYear, currentSeason) = ScheduleViewModel.currentAnimeSeason()
        val seasons = listOf(
            Season(id = 1, malId = 1, title = "Late Show", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.SATURDAY, time = LocalTime.of(23, 30)), isInWatchlist = true, airingSeasonName = currentSeason.name.lowercase(), airingSeasonYear = currentYear),
            Season(id = 2, malId = 2, title = "Early Show", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.SATURDAY, time = LocalTime.of(9, 0)), isInWatchlist = true, airingSeasonName = currentSeason.name.lowercase(), airingSeasonYear = currentYear),
            Season(id = 3, malId = 3, title = "Mid Show", broadcastTime = BroadcastTime(dayOfWeek = DayOfWeek.SATURDAY, time = LocalTime.of(17, 0)), isInWatchlist = true, airingSeasonName = currentSeason.name.lowercase(), airingSeasonYear = currentYear)
        )
        every { observeScheduleUseCase() } returns flowOf(seasons)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val saturdayIds = viewModel.uiState.value.schedule[AnimeDayOfWeek.SATURDAY]?.map { it.id }

        assertThat(saturdayIds).containsExactly(2L, 3L, 1L).inOrder()
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
