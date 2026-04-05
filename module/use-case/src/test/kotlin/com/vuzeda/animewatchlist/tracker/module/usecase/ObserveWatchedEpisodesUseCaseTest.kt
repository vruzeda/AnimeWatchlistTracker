package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveWatchedEpisodesUseCaseTest {

    private val repository = mockk<SeasonRepository>()
    private val useCase = ObserveWatchedEpisodesUseCase(repository)

    @Test
    fun `emits watched episode numbers from repository`() = runTest {
        every { repository.observeWatchedEpisodesForSeason(1L) } returns flowOf(setOf(1, 2, 5))

        useCase(1L).test {
            val result = awaitItem()

            assertThat(result).containsExactly(1, 2, 5)
            awaitComplete()
        }

        verify { repository.observeWatchedEpisodesForSeason(1L) }
    }

    @Test
    fun `emits empty set when no episodes are watched`() = runTest {
        every { repository.observeWatchedEpisodesForSeason(99L) } returns flowOf(emptySet())

        useCase(99L).test {
            val result = awaitItem()

            assertThat(result).isEmpty()
            awaitComplete()
        }
    }
}
