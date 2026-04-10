package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class BackfillMissingAiringSeasonUseCaseTest {

    private val animeRepository = mockk<AnimeRepository>()
    private val seasonRepository = mockk<SeasonRepository>(relaxed = true)
    private val useCase = BackfillMissingAiringSeasonUseCase(animeRepository, seasonRepository)

    private val apiDetails = AnimeFullDetails(
        malId = 100,
        title = "Re:ZERO",
        type = "TV",
        episodes = 13,
        airingSeasonName = "spring",
        airingSeasonYear = 2016,
        broadcastDay = "Wednesdays",
        broadcastTime = "19:00",
        broadcastTimezone = "Asia/Tokyo",
        sequels = emptyList()
    )

    @Test
    fun `skips seasons that already have airingSeasonName populated`() = runTest {
        val alreadyFilled = Season(
            id = 1, malId = 100, title = "Already filled",
            isInWatchlist = true, airingSeasonName = "spring"
        )
        coEvery { seasonRepository.observeAllSeasons() } returns flowOf(listOf(alreadyFilled))

        useCase()

        coVerify(exactly = 0) { animeRepository.fetchAnimeFullById(any()) }
    }

    @Test
    fun `skips non-watchlist seasons`() = runTest {
        val notInWatchlist = Season(
            id = 2, malId = 200, title = "Not in watchlist",
            isInWatchlist = false, airingSeasonName = null
        )
        coEvery { seasonRepository.observeAllSeasons() } returns flowOf(listOf(notInWatchlist))

        useCase()

        coVerify(exactly = 0) { animeRepository.fetchAnimeFullById(any()) }
    }

    @Test
    fun `fetches and persists airing season for eligible seasons`() = runTest {
        val season = Season(id = 1, malId = 100, title = "Re:ZERO", isInWatchlist = true, airingSeasonName = null)
        coEvery { seasonRepository.observeAllSeasons() } returns flowOf(listOf(season))
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(apiDetails)

        useCase()

        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        val updated = slot.captured
        assertThat(updated.airingSeasonName).isEqualTo("spring")
        assertThat(updated.airingSeasonYear).isEqualTo(2016)
    }

    @Test
    fun `null-coalesces broadcast fields from API when missing on stored season`() = runTest {
        val season = Season(
            id = 1, malId = 100, title = "Re:ZERO",
            isInWatchlist = true, airingSeasonName = null,
            broadcastDay = null, broadcastTime = null
        )
        coEvery { seasonRepository.observeAllSeasons() } returns flowOf(listOf(season))
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(apiDetails)

        useCase()

        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        val updated = slot.captured
        assertThat(updated.broadcastDay).isEqualTo("Wednesdays")
        assertThat(updated.broadcastTime).isEqualTo("19:00")
    }

    @Test
    fun `preserves existing broadcast fields and does not overwrite with API values`() = runTest {
        val season = Season(
            id = 1, malId = 100, title = "Re:ZERO",
            isInWatchlist = true, airingSeasonName = null,
            broadcastDay = "Saturdays", broadcastTime = "22:00"
        )
        coEvery { seasonRepository.observeAllSeasons() } returns flowOf(listOf(season))
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(apiDetails)

        useCase()

        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        val updated = slot.captured
        assertThat(updated.broadcastDay).isEqualTo("Saturdays")
        assertThat(updated.broadcastTime).isEqualTo("22:00")
    }

    @Test
    fun `fills null imageUrl from API when season has no image`() = runTest {
        val season = Season(id = 1, malId = 100, title = "Re:ZERO", isInWatchlist = true, airingSeasonName = null, imageUrl = null)
        coEvery { seasonRepository.observeAllSeasons() } returns flowOf(listOf(season))
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(apiDetails.copy(imageUrl = "https://cdn.myanimelist.net/images/100.jpg"))

        useCase()

        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        assertThat(slot.captured.imageUrl).isEqualTo("https://cdn.myanimelist.net/images/100.jpg")
    }

    @Test
    fun `preserves existing imageUrl and does not overwrite with API value`() = runTest {
        val season = Season(id = 1, malId = 100, title = "Re:ZERO", isInWatchlist = true, airingSeasonName = null, imageUrl = "https://chiaki.site/media/a/59/100.jpg")
        coEvery { seasonRepository.observeAllSeasons() } returns flowOf(listOf(season))
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(apiDetails.copy(imageUrl = "https://cdn.myanimelist.net/images/100.jpg"))

        useCase()

        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        assertThat(slot.captured.imageUrl).isEqualTo("https://chiaki.site/media/a/59/100.jpg")
    }

    @Test
    fun `continues to next season when API call fails for one season`() = runTest {
        val failing = Season(id = 1, malId = 100, title = "Failing", isInWatchlist = true, airingSeasonName = null)
        val succeeding = Season(id = 2, malId = 200, title = "Succeeding", isInWatchlist = true, airingSeasonName = null)
        coEvery { seasonRepository.observeAllSeasons() } returns flowOf(listOf(failing, succeeding))
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.failure(Exception("Network error"))
        coEvery { animeRepository.fetchAnimeFullById(200) } returns Result.success(apiDetails.copy(malId = 200))

        useCase()

        coVerify(exactly = 1) { seasonRepository.updateSeason(any()) }
    }
}
