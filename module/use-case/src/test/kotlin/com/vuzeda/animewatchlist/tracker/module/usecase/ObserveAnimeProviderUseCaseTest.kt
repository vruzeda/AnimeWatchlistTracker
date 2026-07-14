package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeProvider
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveAnimeProviderUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>()
    private val useCase = ObserveAnimeProviderUseCase(repository)

    @Test
    fun `emits anime provider from repository`() = runTest {
        every { repository.observeAnimeProvider() } returns flowOf(AnimeProvider.MAL)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(AnimeProvider.MAL)
            awaitComplete()
        }

        verify { repository.observeAnimeProvider() }
    }

    @Test
    fun `emits JIKAN as default provider`() = runTest {
        every { repository.observeAnimeProvider() } returns flowOf(AnimeProvider.JIKAN)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(AnimeProvider.JIKAN)
            awaitComplete()
        }
    }
}
