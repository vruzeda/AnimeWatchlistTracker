package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetEpisodeWatchedUseCaseTest {

    private val repository = mockk<SeasonRepository>(relaxed = true)
    private val useCase = SetEpisodeWatchedUseCase(repository)

    @Test
    fun `delegates to repository with watched true`() = runTest {
        useCase(seasonId = 1L, episodeNumber = 3, isWatched = true)

        coVerify { repository.setEpisodeWatched(1L, 3, true) }
    }

    @Test
    fun `delegates to repository with watched false`() = runTest {
        useCase(seasonId = 1L, episodeNumber = 3, isWatched = false)

        coVerify { repository.setEpisodeWatched(1L, 3, false) }
    }
}
