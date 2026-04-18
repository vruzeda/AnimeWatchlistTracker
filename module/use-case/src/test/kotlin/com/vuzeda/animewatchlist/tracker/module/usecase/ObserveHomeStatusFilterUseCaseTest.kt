package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveHomeStatusFilterUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>()
    private val useCase = ObserveHomeStatusFilterUseCase(repository)

    @Test
    fun `returns empty set when no filter is set`() = runTest {
        every { repository.observeHomeStatusFilter() } returns flowOf(emptySet())

        useCase().test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `returns set with single watch status when filter is set`() = runTest {
        every { repository.observeHomeStatusFilter() } returns flowOf(setOf(WatchStatus.WATCHING))

        useCase().test {
            assertThat(awaitItem()).isEqualTo(setOf(WatchStatus.WATCHING))
            awaitComplete()
        }
    }

    @Test
    fun `returns set with multiple watch statuses when multiple filters are set`() = runTest {
        every { repository.observeHomeStatusFilter() } returns flowOf(setOf(WatchStatus.WATCHING, WatchStatus.COMPLETED))

        useCase().test {
            assertThat(awaitItem()).isEqualTo(setOf(WatchStatus.WATCHING, WatchStatus.COMPLETED))
            awaitComplete()
        }
    }
}
