package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SequelInfo
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CheckAnimeUpdatesUseCaseTest {

    private val repository = mockk<AnimeRepository>(relaxed = true)
    private val seasonRepository = mockk<SeasonRepository>(relaxed = true)
    private val remoteRepository = mockk<AnimeRemoteDataSource>(relaxed = true)
    private val useCase = CheckAnimeUpdatesUseCase(repository, seasonRepository, remoteRepository)

    private val sampleAnime = Anime(
        id = 1,
        title = "Test Anime",
        status = WatchStatus.WATCHING,
        notificationType = NotificationType.BOTH
    )

    private val sampleSeason = Season(
        id = 10L,
        animeId = 1L,
        malId = 100,
        title = "Test Anime Season 1",
        type = "TV",
        orderIndex = 0,
        lastCheckedAiredEpisodeCount = 12
    )

    @Test
    fun `returns empty list when no notified anime`() = runTest {
        coEvery { repository.getNotificationEnabledAnime() } returns emptyList()

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `returns empty list when anime has no seasons`() = runTest {
        coEvery { repository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns emptyList()

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `detects new episodes when aired episode count increased`() = runTest {
        coEvery { repository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { remoteRepository.fetchLastAiredEpisodeNumber(100) } returns Result.success(15)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                title = "Test",
                type = "TV",
                episodes = 25,
                sequels = emptyList()
            )
        )

        val updates = useCase()

        val episodeUpdates = updates.filterIsInstance<AnimeUpdate.NewEpisodes>()
        assertThat(episodeUpdates).hasSize(1)
        assertThat(episodeUpdates[0].latestAiredEpisode).isEqualTo(15)
        assertThat(episodeUpdates[0].anime).isEqualTo(sampleAnime)
        assertThat(episodeUpdates[0].season).isEqualTo(sampleSeason)
    }

    @Test
    fun `does not detect new episodes when aired count unchanged`() = runTest {
        coEvery { repository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { remoteRepository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                title = "Test",
                type = "TV",
                episodes = 25,
                sequels = emptyList()
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).isEmpty()
    }

    @Test
    fun `does not detect new episodes on first check`() = runTest {
        val firstCheckSeason = sampleSeason.copy(lastCheckedAiredEpisodeCount = null)
        coEvery { repository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(firstCheckSeason)
        coEvery { remoteRepository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                title = "Test",
                type = "TV",
                episodes = 25,
                sequels = emptyList()
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).isEmpty()
    }

    @Test
    fun `detects new season when sequel is currently airing`() = runTest {
        coEvery { repository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { remoteRepository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                title = "Test",
                type = "TV",
                episodes = 25,
                sequels = listOf(SequelInfo(300, "New Season"))
            )
        )
        coEvery { remoteRepository.fetchAnimeFullById(300) } returns Result.success(
            AnimeFullDetails(
                malId = 300,
                title = "New Season",
                type = "TV",
                episodes = null,
                airingStatus = "Currently Airing",
                sequels = emptyList()
            )
        )

        val updates = useCase()

        val seasonUpdates = updates.filterIsInstance<AnimeUpdate.NewSeason>()
        assertThat(seasonUpdates).hasSize(1)
        assertThat(seasonUpdates[0].sequelMalId).isEqualTo(300)
        assertThat(seasonUpdates[0].sequelTitle).isEqualTo("New Season")
    }

    @Test
    fun `does not notify for sequel that is not yet confirmed`() = runTest {
        coEvery { repository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { remoteRepository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                title = "Test",
                type = "TV",
                episodes = 25,
                sequels = listOf(SequelInfo(300, "Unconfirmed"))
            )
        )
        coEvery { remoteRepository.fetchAnimeFullById(300) } returns Result.success(
            AnimeFullDetails(
                malId = 300,
                title = "Unconfirmed",
                type = "TV",
                episodes = null,
                airingStatus = "Not yet aired",
                sequels = emptyList()
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).isEmpty()
    }

    @Test
    fun `updates season notification data after checking episodes`() = runTest {
        coEvery { repository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { remoteRepository.fetchLastAiredEpisodeNumber(100) } returns Result.success(15)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                title = "Test",
                type = "TV",
                episodes = 25,
                sequels = emptyList()
            )
        )

        useCase()

        coVerify {
            seasonRepository.updateSeasonNotificationData(
                seasonId = 10L,
                lastCheckedAiredEpisodeCount = 15
            )
        }
    }
}
