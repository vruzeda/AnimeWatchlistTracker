package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateSchedulerState
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ObserveAnimeUpdateSchedulerStateUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = ObserveAnimeUpdateSchedulerStateUseCase(repository)

    @Test
    fun `emits scheduler state from repository`() = runTest {
        val state = AnimeUpdateSchedulerState(
            lastSuccessfulRunAt = Instant.fromEpochMilliseconds(1_000_000L),
            lastAttemptAt = Instant.fromEpochMilliseconds(2_000_000L),
            lastAttemptResult = AnimeUpdateResult.Success
        )
        every { repository.observeAnimeUpdateSchedulerState() } returns flowOf(state)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(state)
            awaitComplete()
        }
    }

    @Test
    fun `emits state with null fields when never run`() = runTest {
        val state = AnimeUpdateSchedulerState(
            lastSuccessfulRunAt = null,
            lastAttemptAt = null,
            lastAttemptResult = null
        )
        every { repository.observeAnimeUpdateSchedulerState() } returns flowOf(state)

        useCase().test {
            val result = awaitItem()
            assertThat(result.lastSuccessfulRunAt).isNull()
            assertThat(result.lastAttemptAt).isNull()
            assertThat(result.lastAttemptResult).isNull()
            awaitComplete()
        }
    }
}
