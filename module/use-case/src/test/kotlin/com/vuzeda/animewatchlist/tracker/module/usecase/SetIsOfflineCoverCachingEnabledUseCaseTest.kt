package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetIsOfflineCoverCachingEnabledUseCaseTest {

    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxUnitFun = true)
    private val useCase = SetIsOfflineCoverCachingEnabledUseCase(userPreferencesRepository)

    @Test
    fun `delegates enabled state to repository`() = runTest {
        useCase(true)

        coVerify(exactly = 1) { userPreferencesRepository.setIsOfflineCoverCachingEnabled(true) }
    }

    @Test
    fun `delegates disabled state to repository`() = runTest {
        useCase(false)

        coVerify(exactly = 1) { userPreferencesRepository.setIsOfflineCoverCachingEnabled(false) }
    }
}
