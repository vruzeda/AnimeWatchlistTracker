package com.vuzeda.animewatchlist.tracker.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveWatchlistMalIdsUseCaseTest {

    private val repository = mockk<SeasonRepository>()
    private val useCase = ObserveWatchlistMalIdsUseCase(repository)

    @Test
    fun `emits set of malIds from repository`() = runTest {
        every { repository.observeAllSeasonMalIds() } returns flowOf(setOf(1, 2, 3))

        useCase().test {
            val result = awaitItem()

            assertThat(result).containsExactly(1, 2, 3)
            awaitComplete()
        }

        verify { repository.observeAllSeasonMalIds() }
    }

    @Test
    fun `emits empty set when no seasons exist`() = runTest {
        every { repository.observeAllSeasonMalIds() } returns flowOf(emptySet())

        useCase().test {
            val result = awaitItem()

            assertThat(result).isEmpty()
            awaitComplete()
        }
    }
}
