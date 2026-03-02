package com.vuzeda.animewatchlist.tracker.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.HomeViewMode
import com.vuzeda.animewatchlist.tracker.domain.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveHomeViewModeUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>()
    private val useCase = ObserveHomeViewModeUseCase(repository)

    @Test
    fun `emits home view mode from repository`() = runTest {
        every { repository.observeHomeViewMode() } returns flowOf(HomeViewMode.SEASON)

        useCase().test {
            val result = awaitItem()

            assertThat(result).isEqualTo(HomeViewMode.SEASON)
            awaitComplete()
        }

        verify { repository.observeHomeViewMode() }
    }

    @Test
    fun `emits ANIME as default mode`() = runTest {
        every { repository.observeHomeViewMode() } returns flowOf(HomeViewMode.ANIME)

        useCase().test {
            val result = awaitItem()

            assertThat(result).isEqualTo(HomeViewMode.ANIME)
            awaitComplete()
        }
    }
}
