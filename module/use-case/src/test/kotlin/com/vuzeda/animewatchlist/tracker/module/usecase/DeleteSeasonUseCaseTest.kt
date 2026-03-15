package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coJustRun
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteSeasonUseCaseTest {

    private val seasonRepository: SeasonRepository = mockk()
    private val animeRepository: AnimeRepository = mockk()
    private val useCase = DeleteSeasonUseCase(seasonRepository, animeRepository)

    private val season = Season(id = 1L, animeId = 10L, malId = 100, title = "S1", isInWatchlist = true)

    @Test
    fun `deletes only the season when other in-watchlist seasons remain`() = runTest {
        val sibling = Season(id = 2L, animeId = 10L, malId = 200, title = "S2", isInWatchlist = true)
        coEvery { seasonRepository.getSeasonsForAnime(10L) } returns listOf(season, sibling)
        coJustRun { seasonRepository.deleteSeason(1L) }

        useCase(season)

        coVerify { seasonRepository.deleteSeason(1L) }
        coVerify(exactly = 0) { animeRepository.deleteAnime(any()) }
    }

    @Test
    fun `deletes the parent anime when the season is the last in-watchlist season`() = runTest {
        coEvery { seasonRepository.getSeasonsForAnime(10L) } returns listOf(season)
        coJustRun { animeRepository.deleteAnime(10L) }

        useCase(season)

        coVerify { animeRepository.deleteAnime(10L) }
        coVerify(exactly = 0) { seasonRepository.deleteSeason(any()) }
    }

    @Test
    fun `deletes the parent anime when only non-watchlist seasons remain besides the deleted one`() = runTest {
        val nonWatchlistSibling = Season(id = 2L, animeId = 10L, malId = 200, title = "S2", isInWatchlist = false)
        coEvery { seasonRepository.getSeasonsForAnime(10L) } returns listOf(season, nonWatchlistSibling)
        coJustRun { animeRepository.deleteAnime(10L) }

        useCase(season)

        coVerify { animeRepository.deleteAnime(10L) }
        coVerify(exactly = 0) { seasonRepository.deleteSeason(any()) }
    }
}
