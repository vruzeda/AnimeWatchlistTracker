package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeProvider
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetAnimeProviderUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>()
    private val useCase = SetAnimeProviderUseCase(repository)

    @Test
    fun `delegates provider to repository`() = runTest {
        coEvery { repository.setAnimeProvider(any()) } returns Unit

        useCase(AnimeProvider.MAL)

        coVerify { repository.setAnimeProvider(AnimeProvider.MAL) }
    }
}
