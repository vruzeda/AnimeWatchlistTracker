package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeProvider
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveIsSearchFilteringAvailableUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>()
    private val useCase = ObserveIsSearchFilteringAvailableUseCase(repository)

    @Test
    fun `emits true when Jikan is the selected provider`() = runTest {
        every { repository.observeAnimeProvider() } returns flowOf(AnimeProvider.JIKAN)

        useCase().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `emits false when MAL is the selected provider`() = runTest {
        every { repository.observeAnimeProvider() } returns flowOf(AnimeProvider.MAL)

        useCase().test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `re-emits availability when the provider changes`() = runTest {
        every { repository.observeAnimeProvider() } returns
            flowOf(AnimeProvider.JIKAN, AnimeProvider.MAL)

        useCase().test {
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }
}
