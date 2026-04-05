package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetAllEpisodesWatchedUseCaseTest {

    private val repository = mockk<SeasonRepository>(relaxed = true)
    private val useCase = SetAllEpisodesWatchedUseCase(repository)

    @Test
    fun `marks each episode as watched`() = runTest {
        useCase(seasonId = 1L, episodeNumbers = listOf(1, 2, 3))

        coVerify { repository.setEpisodeWatched(1L, 1, true) }
        coVerify { repository.setEpisodeWatched(1L, 2, true) }
        coVerify { repository.setEpisodeWatched(1L, 3, true) }
    }

    @Test
    fun `does nothing when episode list is empty`() = runTest {
        useCase(seasonId = 1L, episodeNumbers = emptyList())

        coVerify(exactly = 0) { repository.setEpisodeWatched(any(), any(), any()) }
    }
}
