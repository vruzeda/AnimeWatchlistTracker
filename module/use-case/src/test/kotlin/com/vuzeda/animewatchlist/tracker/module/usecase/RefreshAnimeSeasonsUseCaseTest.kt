package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RefreshAnimeSeasonsUseCaseTest {

    private val animeRepository: AnimeRepository = mockk()
    private val seasonRepository: SeasonRepository = mockk(relaxed = true)
    private val useCase = RefreshAnimeSeasonsUseCase(animeRepository, seasonRepository)

    @Test
    fun `does nothing when anime has no seasons`() = runTest {
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns emptyList()

        useCase(1L)

        coVerify(exactly = 0) { animeRepository.fetchWatchOrder(any()) }
        coVerify(exactly = 0) { seasonRepository.addSeasonsToAnime(any(), any()) }
    }

    @Test
    fun `does nothing when watch order fetch fails`() = runTest {
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(
            Season(malId = 100, title = "S1")
        )
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.failure(Exception("Network error"))

        useCase(1L)

        coVerify(exactly = 0) { seasonRepository.addSeasonsToAnime(any(), any()) }
    }

    @Test
    fun `adds missing seasons from watch order as non-watchlist`() = runTest {
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(
            Season(malId = 100, title = "Anime S1")
        )
        val watchOrder = listOf(
            SeasonData(malId = 100, title = "Anime S1", type = "TV"),
            SeasonData(malId = 200, title = "Anime S2", type = "TV", episodeCount = 24, score = 9.0)
        )
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(watchOrder)

        useCase(1L)

        val slot = slot<List<Season>>()
        coVerify { seasonRepository.addSeasonsToAnime(eq(1L), capture(slot)) }
        assertThat(slot.captured).hasSize(1)
        assertThat(slot.captured[0].malId).isEqualTo(200)
        assertThat(slot.captured[0].isInWatchlist).isFalse()
        assertThat(slot.captured[0].orderIndex).isEqualTo(1)
        assertThat(slot.captured[0].episodeCount).isEqualTo(24)
        assertThat(slot.captured[0].score).isEqualTo(9.0)
    }

    @Test
    fun `does nothing when all watch order seasons already exist locally`() = runTest {
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(
            Season(malId = 100, title = "Anime S1"),
            Season(malId = 200, title = "Anime S2")
        )
        val watchOrder = listOf(
            SeasonData(malId = 100, title = "Anime S1", type = "TV"),
            SeasonData(malId = 200, title = "Anime S2", type = "TV")
        )
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(watchOrder)

        useCase(1L)

        coVerify(exactly = 0) { seasonRepository.addSeasonsToAnime(any(), any()) }
    }
}
