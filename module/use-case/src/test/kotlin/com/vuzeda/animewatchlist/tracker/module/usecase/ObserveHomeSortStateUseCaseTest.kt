package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortOption
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortState
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveHomeSortStateUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>()
    private val useCase = ObserveHomeSortStateUseCase(repository)

    @Test
    fun `delegates to repository and returns sort state`() = runTest {
        val expected = HomeSortState(HomeSortOption.USER_RATING, false)
        every { repository.observeHomeSortState() } returns flowOf(expected)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(expected)
            awaitComplete()
        }
    }

    @Test
    fun `returns default sort state when repository emits default`() = runTest {
        val expected = HomeSortState()
        every { repository.observeHomeSortState() } returns flowOf(expected)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(expected)
            awaitComplete()
        }
    }
}
