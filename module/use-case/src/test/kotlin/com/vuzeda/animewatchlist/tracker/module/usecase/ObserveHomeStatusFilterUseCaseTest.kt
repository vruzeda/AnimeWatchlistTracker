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
    fun `returns null when no filter is set`() = runTest {
        every { repository.observeHomeStatusFilter() } returns flowOf(null)

        useCase().test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `returns watch status when filter is set`() = runTest {
        every { repository.observeHomeStatusFilter() } returns flowOf(WatchStatus.WATCHING)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(WatchStatus.WATCHING)
            awaitComplete()
        }
    }
}
