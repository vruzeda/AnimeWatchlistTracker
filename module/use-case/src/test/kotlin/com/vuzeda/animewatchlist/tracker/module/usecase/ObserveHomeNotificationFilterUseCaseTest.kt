package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveHomeNotificationFilterUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>()
    private val useCase = ObserveHomeNotificationFilterUseCase(repository)

    @Test
    fun `returns null when no filter is set`() = runTest {
        every { repository.observeHomeNotificationFilter() } returns flowOf(null)

        useCase().test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `returns true when notification filter is enabled`() = runTest {
        every { repository.observeHomeNotificationFilter() } returns flowOf(true)

        useCase().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `returns false when notification filter is disabled`() = runTest {
        every { repository.observeHomeNotificationFilter() } returns flowOf(false)

        useCase().test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }
}
