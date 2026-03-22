package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ObserveLastAnimeUpdateRunUseCaseTest {

    private val repository = mockk<SchedulerRepository>()
    private val useCase = ObserveLastAnimeUpdateRunUseCase(repository)

    @Test
    fun `emits null when no run recorded`() = runTest {
        every { repository.observeLastAnimeUpdateRun() } returns flowOf(null)

        useCase().test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `emits Instant when run has been recorded`() = runTest {
        val instant = Instant.fromEpochMilliseconds(1_000_000L)
        every { repository.observeLastAnimeUpdateRun() } returns flowOf(instant)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(instant)
            awaitComplete()
        }
    }
}
