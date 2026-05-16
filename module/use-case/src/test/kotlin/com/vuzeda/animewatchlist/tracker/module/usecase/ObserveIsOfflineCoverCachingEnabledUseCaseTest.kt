package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveIsOfflineCoverCachingEnabledUseCaseTest {

    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val useCase = ObserveIsOfflineCoverCachingEnabledUseCase(userPreferencesRepository)

    @Test
    fun `emits offline cover caching enabled state from repository`() = runTest {
        every { userPreferencesRepository.observeIsOfflineCoverCachingEnabled() } returns flowOf(true)

        useCase().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `emits false when offline cover caching is disabled`() = runTest {
        every { userPreferencesRepository.observeIsOfflineCoverCachingEnabled() } returns flowOf(false)

        useCase().test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }
}
