package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetInstallationIdUseCaseTest {

    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val useCase = GetInstallationIdUseCase(userPreferencesRepository)

    @Test
    fun `get installation id from repository`() = runTest {
        val installationId = "InstallationId"
        coEvery { userPreferencesRepository.getInstallationId() } returns installationId

        val result = useCase()

        assertThat(result).isEqualTo(installationId)
        coVerify { userPreferencesRepository.getInstallationId() }
    }
}
