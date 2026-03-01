package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AddSeasonsToAnimeUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = AddSeasonsToAnimeUseCase(repository)

    @Test
    fun `delegates to repository with correct animeId and seasons`() = runTest {
        val seasons = listOf(
            Season(malId = 100, title = "Season 1", orderIndex = 0),
            Season(malId = 101, title = "Season 2", orderIndex = 1)
        )
        coEvery { repository.addSeasonsToAnime(5L, seasons) } returns Unit

        useCase(5L, seasons)

        coVerify { repository.addSeasonsToAnime(5L, seasons) }
    }
}
