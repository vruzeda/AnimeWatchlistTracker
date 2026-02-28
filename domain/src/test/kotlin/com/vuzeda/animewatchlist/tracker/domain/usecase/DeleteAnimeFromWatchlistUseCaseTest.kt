package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteAnimeFromWatchlistUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = DeleteAnimeFromWatchlistUseCase(repository)

    @Test
    fun `delegates deletion to repository`() = runTest {
        coEvery { repository.deleteAnime(1L) } returns Unit

        useCase(1L)

        coVerify { repository.deleteAnime(1L) }
    }
}
