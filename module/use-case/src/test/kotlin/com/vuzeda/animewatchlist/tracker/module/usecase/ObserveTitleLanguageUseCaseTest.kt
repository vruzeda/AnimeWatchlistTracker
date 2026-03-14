package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveTitleLanguageUseCaseTest {

    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val useCase = ObserveTitleLanguageUseCase(userPreferencesRepository)

    @Test
    fun `emits title language from repository`() = runTest {
        every { userPreferencesRepository.observeTitleLanguage() } returns flowOf(TitleLanguage.ENGLISH)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(TitleLanguage.ENGLISH)
            awaitComplete()
        }
    }
}
