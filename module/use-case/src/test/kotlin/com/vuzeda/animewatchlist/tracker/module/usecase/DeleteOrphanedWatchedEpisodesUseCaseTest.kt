package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteOrphanedWatchedEpisodesUseCaseTest {

    private val repository = mockk<SeasonRepository>(relaxed = true)
    private val useCase = DeleteOrphanedWatchedEpisodesUseCase(repository)

    @Test
    fun `delegates to repository with season id and episode count`() = runTest {
        useCase(seasonId = 7L, episodeCount = 24)

        coVerify { repository.deleteOrphanedWatchedEpisodes(7L, 24) }
    }
}
