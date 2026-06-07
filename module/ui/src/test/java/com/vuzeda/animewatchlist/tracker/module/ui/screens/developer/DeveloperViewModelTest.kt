package com.vuzeda.animewatchlist.tracker.module.ui.screens.developer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateSchedulerState
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAnimeUpdateSchedulerStateUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveIsNotificationDebugInfoEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetIsDeveloperOptionsEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetIsNotificationDebugInfoEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.TriggerAnimeUpdateUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DeveloperViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeAnimeUpdateSchedulerStateUseCase: ObserveAnimeUpdateSchedulerStateUseCase = mockk()
    private val triggerAnimeUpdateUseCase: TriggerAnimeUpdateUseCase = mockk(relaxUnitFun = true)
    private val setIsDeveloperOptionsEnabledUseCase: SetIsDeveloperOptionsEnabledUseCase = mockk(relaxUnitFun = true)
    private val observeIsNotificationDebugInfoEnabledUseCase: ObserveIsNotificationDebugInfoEnabledUseCase = mockk()
    private val setIsNotificationDebugInfoEnabledUseCase: SetIsNotificationDebugInfoEnabledUseCase = mockk(relaxUnitFun = true)

    private val emptySchedulerState = AnimeUpdateSchedulerState(
        lastSuccessfulRunAt = null,
        lastAttemptAt = null,
        lastAttemptResult = null
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeAnimeUpdateSchedulerStateUseCase() } returns flowOf(emptySchedulerState)
        every { observeIsNotificationDebugInfoEnabledUseCase() } returns flowOf(false)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DeveloperViewModel(
        observeAnimeUpdateSchedulerStateUseCase,
        triggerAnimeUpdateUseCase,
        setIsDeveloperOptionsEnabledUseCase,
        observeIsNotificationDebugInfoEnabledUseCase,
        setIsNotificationDebugInfoEnabledUseCase
    )

    @Test
    fun `initial state has null lastAnimeUpdateRun`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.lastAnimeUpdateRun).isNull()
        }
    }

    @Test
    fun `initial state has null lastAnimeUpdateAttemptAt`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.lastAnimeUpdateAttemptAt).isNull()
        }
    }

    @Test
    fun `initial state has null lastAnimeUpdateAttemptResult`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.lastAnimeUpdateAttemptResult).isNull()
        }
    }

    @Test
    fun `updates scheduler state fields when use case emits`() = runTest {
        val successRunInstant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
        val attemptInstant = Instant.fromEpochMilliseconds(1_700_000_001_000L)
        val state = AnimeUpdateSchedulerState(
            lastSuccessfulRunAt = successRunInstant,
            lastAttemptAt = attemptInstant,
            lastAttemptResult = AnimeUpdateResult.Success
        )
        every { observeAnimeUpdateSchedulerStateUseCase() } returns flowOf(state)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            val updated = awaitItem()
            assertThat(updated.lastAnimeUpdateRun).isEqualTo(successRunInstant)
            assertThat(updated.lastAnimeUpdateAttemptAt).isEqualTo(attemptInstant)
            assertThat(updated.lastAnimeUpdateAttemptResult).isEqualTo(AnimeUpdateResult.Success)
        }
    }

    @Test
    fun `maps failure result to ui state`() = runTest {
        val state = AnimeUpdateSchedulerState(
            lastSuccessfulRunAt = null,
            lastAttemptAt = Instant.fromEpochMilliseconds(1_000_000L),
            lastAttemptResult = AnimeUpdateResult.Failure("timeout")
        )
        every { observeAnimeUpdateSchedulerStateUseCase() } returns flowOf(state)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            val updated = awaitItem()
            assertThat(updated.lastAnimeUpdateAttemptResult).isEqualTo(AnimeUpdateResult.Failure("timeout"))
        }
    }

    @Test
    fun `maps retry result to ui state`() = runTest {
        val state = AnimeUpdateSchedulerState(
            lastSuccessfulRunAt = null,
            lastAttemptAt = Instant.fromEpochMilliseconds(1_000_000L),
            lastAttemptResult = AnimeUpdateResult.WillRetry
        )
        every { observeAnimeUpdateSchedulerStateUseCase() } returns flowOf(state)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            val updated = awaitItem()
            assertThat(updated.lastAnimeUpdateAttemptResult).isEqualTo(AnimeUpdateResult.WillRetry)
        }
    }

    @Test
    fun `triggerAnimeUpdate delegates to use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.triggerAnimeUpdate()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { triggerAnimeUpdateUseCase() }
    }

    @Test
    fun `disableDeveloperOptions persists disabled state`() = runTest {
        val viewModel = createViewModel()

        viewModel.disableDeveloperOptions()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { setIsDeveloperOptionsEnabledUseCase(false) }
    }

    @Test
    fun `initial state has notification debug info disabled`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.isNotificationDebugInfoEnabled).isFalse()
        }
    }

    @Test
    fun `updates isNotificationDebugInfoEnabled when use case emits true`() = runTest {
        every { observeIsNotificationDebugInfoEnabledUseCase() } returns flowOf(true)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            val updated = awaitItem()
            assertThat(updated.isNotificationDebugInfoEnabled).isTrue()
        }
    }

    @Test
    fun `toggleNotificationDebugInfo enables debug info when currently disabled`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.toggleNotificationDebugInfo()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { setIsNotificationDebugInfoEnabledUseCase(true) }
    }

    @Test
    fun `toggleNotificationDebugInfo disables debug info when currently enabled`() = runTest {
        every { observeIsNotificationDebugInfoEnabledUseCase() } returns flowOf(true)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.toggleNotificationDebugInfo()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { setIsNotificationDebugInfoEnabledUseCase(false) }
    }
}
