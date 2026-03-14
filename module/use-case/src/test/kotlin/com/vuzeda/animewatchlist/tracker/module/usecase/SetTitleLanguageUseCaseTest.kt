package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetTitleLanguageUseCaseTest {

    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val useCase = SetTitleLanguageUseCase(userPreferencesRepository)

    @Test
    fun `delegates language update to repository`() = runTest {
        coJustRun { userPreferencesRepository.setTitleLanguage(TitleLanguage.JAPANESE) }

        useCase(TitleLanguage.JAPANESE)

        coVerify { userPreferencesRepository.setTitleLanguage(TitleLanguage.JAPANESE) }
    }
}
