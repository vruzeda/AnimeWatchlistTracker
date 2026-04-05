package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.StreamingInfo
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RefreshSeasonDataUseCaseTest {

    private val animeRepository: AnimeRepository = mockk()
    private val seasonRepository: SeasonRepository = mockk(relaxed = true)
    private val useCase = RefreshSeasonDataUseCase(animeRepository, seasonRepository)

    private val existingSeason = Season(
        id = 1L,
        animeId = 10L,
        malId = 16498,
        title = "Attack on Titan",
        type = "TV",
        episodeCount = 25,
        score = 8.4,
        orderIndex = 0,
        isInWatchlist = true
    )

    private val apiDetails = AnimeFullDetails(
        malId = 16498,
        title = "Attack on Titan",
        titleEnglish = "Attack on Titan",
        titleJapanese = "進撃の巨人",
        imageUrl = "https://cdn.myanimelist.net/new.jpg",
        type = "TV",
        episodes = 25,
        score = 8.54,
        airingStatus = "Finished Airing",
        broadcastInfo = "Saturdays at 22:00",
        broadcastDay = "Saturdays",
        broadcastTime = "22:00",
        broadcastTimezone = "Asia/Tokyo",
        streamingLinks = listOf(StreamingInfo("Crunchyroll", "https://crunchyroll.com/aot")),
        sequels = emptyList()
    )

    @Test
    fun `updates season with fresh broadcast and streaming data from API`() = runTest {
        coEvery { animeRepository.fetchAnimeFullById(16498) } returns Result.success(apiDetails)

        val result = useCase(existingSeason)

        assertThat(result.isSuccess).isTrue()
        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        val updated = slot.captured
        assertThat(updated.broadcastInfo).isEqualTo("Saturdays at 22:00")
        assertThat(updated.broadcastDay).isEqualTo("Saturdays")
        assertThat(updated.broadcastTime).isEqualTo("22:00")
        assertThat(updated.broadcastTimezone).isEqualTo("Asia/Tokyo")
        assertThat(updated.streamingLinks).hasSize(1)
        assertThat(updated.streamingLinks[0].name).isEqualTo("Crunchyroll")
    }

    @Test
    fun `preserves user-specific fields when updating`() = runTest {
        coEvery { animeRepository.fetchAnimeFullById(16498) } returns Result.success(apiDetails)

        useCase(existingSeason)

        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        val updated = slot.captured
        assertThat(updated.id).isEqualTo(1L)
        assertThat(updated.animeId).isEqualTo(10L)
        assertThat(updated.malId).isEqualTo(16498)
        assertThat(updated.orderIndex).isEqualTo(0)
        assertThat(updated.isInWatchlist).isTrue()
    }

    @Test
    fun `updates title and metadata from API`() = runTest {
        coEvery { animeRepository.fetchAnimeFullById(16498) } returns Result.success(apiDetails)

        useCase(existingSeason)

        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        val updated = slot.captured
        assertThat(updated.titleEnglish).isEqualTo("Attack on Titan")
        assertThat(updated.titleJapanese).isEqualTo("進撃の巨人")
        assertThat(updated.imageUrl).isEqualTo("https://cdn.myanimelist.net/new.jpg")
        assertThat(updated.score).isEqualTo(8.54)
    }

    @Test
    fun `falls back to existing imageUrl when API returns null`() = runTest {
        val detailsWithNullImage = apiDetails.copy(imageUrl = null)
        coEvery { animeRepository.fetchAnimeFullById(16498) } returns Result.success(detailsWithNullImage)
        val seasonWithImage = existingSeason.copy(imageUrl = "https://existing.com/image.jpg")

        useCase(seasonWithImage)

        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        assertThat(slot.captured.imageUrl).isEqualTo("https://existing.com/image.jpg")
    }

    @Test
    fun `falls back to existing episodeCount when API returns null`() = runTest {
        val detailsWithNullEpisodes = apiDetails.copy(episodes = null)
        coEvery { animeRepository.fetchAnimeFullById(16498) } returns Result.success(detailsWithNullEpisodes)

        useCase(existingSeason)

        val slot = slot<Season>()
        coVerify { seasonRepository.updateSeason(capture(slot)) }
        assertThat(slot.captured.episodeCount).isEqualTo(25)
    }

    @Test
    fun `returns failure and skips DB update when API call fails`() = runTest {
        coEvery { animeRepository.fetchAnimeFullById(16498) } returns Result.failure(Exception("Network error"))

        val result = useCase(existingSeason)

        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { seasonRepository.updateSeason(any()) }
    }
}
