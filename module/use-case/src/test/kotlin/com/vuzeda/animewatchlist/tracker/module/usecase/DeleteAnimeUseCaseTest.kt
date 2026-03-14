package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteAnimeUseCaseTest {

    private val animeRepository: AnimeRepository = mockk()
    private val useCase = DeleteAnimeUseCase(animeRepository)

    @Test
    fun `delegates deletion to repository`() = runTest {
        coJustRun { animeRepository.deleteAnime(7L) }

        useCase(7L)

        coVerify { animeRepository.deleteAnime(7L) }
    }
}
