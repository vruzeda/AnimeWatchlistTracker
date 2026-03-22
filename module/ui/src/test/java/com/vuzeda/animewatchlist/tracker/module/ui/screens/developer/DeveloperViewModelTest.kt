package com.vuzeda.animewatchlist.tracker.module.ui.screens.developer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveLastAnimeUpdateRunUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetIsDeveloperOptionsEnabledUseCase
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
    private val observeLastAnimeUpdateRunUseCase: ObserveLastAnimeUpdateRunUseCase = mockk()
    private val triggerAnimeUpdateUseCase: TriggerAnimeUpdateUseCase = mockk(relaxUnitFun = true)
    private val setIsDeveloperOptionsEnabledUseCase: SetIsDeveloperOptionsEnabledUseCase = mockk(relaxUnitFun = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeLastAnimeUpdateRunUseCase() } returns flowOf(null)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DeveloperViewModel(
        observeLastAnimeUpdateRunUseCase,
        triggerAnimeUpdateUseCase,
        setIsDeveloperOptionsEnabledUseCase
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
    fun `updates lastAnimeUpdateRun when use case emits`() = runTest {
        val instant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
        every { observeLastAnimeUpdateRunUseCase() } returns flowOf(instant)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            val updated = awaitItem()
            assertThat(updated.lastAnimeUpdateRun).isEqualTo(instant)
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
}
