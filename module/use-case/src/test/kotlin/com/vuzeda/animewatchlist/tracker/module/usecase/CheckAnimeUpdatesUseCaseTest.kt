package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SequelInfo
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class CheckAnimeUpdatesUseCaseTest {

    private val animeRepository = mockk<AnimeRepository>(relaxed = true)
    private val seasonRepository = mockk<SeasonRepository>(relaxed = true)
    private val fixedDate = LocalDate.of(2026, 3, 15)
    private val fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
    private val useCase = CheckAnimeUpdatesUseCase(animeRepository, seasonRepository, fixedClock)

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
        coEvery { animeRepository.getNotificationEnabledAnime() } returns emptyList()

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `returns empty list when anime has no seasons`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns emptyList()

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `detects new episodes when aired episode count increased`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(15)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
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
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
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
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(firstCheckSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
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
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                title = "Test",
                type = "TV",
                episodes = 25,
                sequels = listOf(SequelInfo(300, "New Season"))
            )
        )
        coEvery { animeRepository.fetchAnimeFullById(300) } returns Result.success(
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
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                title = "Test",
                type = "TV",
                episodes = 25,
                sequels = listOf(SequelInfo(300, "Unconfirmed"))
            )
        )
        coEvery { animeRepository.fetchAnimeFullById(300) } returns Result.success(
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
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(15)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
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

    @Test
    fun `only checks episodes when notification type is NEW_EPISODES`() = runTest {
        val episodesOnlyAnime = sampleAnime.copy(notificationType = NotificationType.NEW_EPISODES)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(episodesOnlyAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(15)

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).hasSize(1)
        coVerify(exactly = 0) { animeRepository.fetchAnimeFullById(any()) }
    }

    @Test
    fun `only checks new seasons when notification type is NEW_SEASONS`() = runTest {
        val seasonsOnlyAnime = sampleAnime.copy(notificationType = NotificationType.NEW_SEASONS)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(seasonsOnlyAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100, title = "Test", type = "TV", episodes = 25,
                sequels = listOf(SequelInfo(300, "New Season"))
            )
        )
        coEvery { animeRepository.fetchAnimeFullById(300) } returns Result.success(
            AnimeFullDetails(
                malId = 300, title = "New Season", type = "TV", episodes = null,
                airingStatus = CheckAnimeUpdatesUseCase.STATUS_CURRENTLY_AIRING, sequels = emptyList()
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).hasSize(1)
        coVerify(exactly = 0) { animeRepository.fetchLastAiredEpisodeNumber(any(), any()) }
    }

    @Test
    fun `skips sequel already present in existing seasons`() = runTest {
        val seasonWithSequel = sampleSeason.copy(malId = 300)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason, seasonWithSequel)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(any(), fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100, title = "Test", type = "TV", episodes = 12,
                sequels = listOf(SequelInfo(300, "Already Added"))
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).isEmpty()
    }

    @Test
    fun `does not notify for sequel when details fetch fails`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100, title = "Test", type = "TV", episodes = 12,
                sequels = listOf(SequelInfo(300, "New Season"))
            )
        )
        coEvery { animeRepository.fetchAnimeFullById(300) } returns Result.failure(Exception("Not found"))

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).isEmpty()
    }

    @Test
    fun `does not notify for sequel when type is not TV or Movie`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100, title = "Test", type = "TV", episodes = 12,
                sequels = listOf(SequelInfo(300, "OVA"))
            )
        )
        coEvery { animeRepository.fetchAnimeFullById(300) } returns Result.success(
            AnimeFullDetails(
                malId = 300, title = "OVA", type = "OVA", episodes = 1,
                airingStatus = CheckAnimeUpdatesUseCase.STATUS_FINISHED_AIRING, sequels = emptyList()
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).isEmpty()
    }

    @Test
    fun `detects new season when sequel has finished airing`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100, title = "Test", type = "TV", episodes = 12,
                sequels = listOf(SequelInfo(300, "Finished Season"))
            )
        )
        coEvery { animeRepository.fetchAnimeFullById(300) } returns Result.success(
            AnimeFullDetails(
                malId = 300, title = "Finished Season", type = "TV", episodes = 12,
                airingStatus = CheckAnimeUpdatesUseCase.STATUS_FINISHED_AIRING, sequels = emptyList()
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).hasSize(1)
    }

    @Test
    fun `checks per-season episode notifications for seasons not covered by anime-level check`() = runTest {
        val perSeasonSeason = Season(
            id = 20L, animeId = 2L, malId = 200, title = "Standalone",
            lastCheckedAiredEpisodeCount = 5
        )
        coEvery { animeRepository.getNotificationEnabledAnime() } returns emptyList()
        coEvery { seasonRepository.getSeasonsWithEpisodeNotifications() } returns listOf(perSeasonSeason)
        coEvery { animeRepository.getAnimeById(2L) } returns Anime(id = 2L, title = "Standalone Anime")
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(200, fixedDate) } returns Result.success(8)

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).hasSize(1)
        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()[0].latestAiredEpisode).isEqualTo(8)
    }

    @Test
    fun `skips per-season check when season was already checked at anime level`() = runTest {
        val episodesOnlyAnime = sampleAnime.copy(notificationType = NotificationType.NEW_EPISODES)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(episodesOnlyAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { seasonRepository.getSeasonsWithEpisodeNotifications() } returns listOf(sampleSeason)

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).isEmpty()
    }

    @Test
    fun `skips per-season notification when anime cannot be found`() = runTest {
        val perSeasonSeason = Season(id = 20L, animeId = 99L, malId = 200, title = "Unknown")
        coEvery { animeRepository.getNotificationEnabledAnime() } returns emptyList()
        coEvery { seasonRepository.getSeasonsWithEpisodeNotifications() } returns listOf(perSeasonSeason)
        coEvery { animeRepository.getAnimeById(99L) } returns null

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `skips per-season notification when no new episodes found`() = runTest {
        val perSeasonSeason = Season(
            id = 20L, animeId = 2L, malId = 200, title = "Standalone",
            lastCheckedAiredEpisodeCount = 8
        )
        coEvery { animeRepository.getNotificationEnabledAnime() } returns emptyList()
        coEvery { seasonRepository.getSeasonsWithEpisodeNotifications() } returns listOf(perSeasonSeason)
        coEvery { animeRepository.getAnimeById(2L) } returns Anime(id = 2L, title = "Standalone Anime")
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(200, fixedDate) } returns Result.success(8)

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).isEmpty()
    }

    @Test
    fun `does not check new seasons when existing seasons list is empty for max by`() = runTest {
        val seasonsOnlyAnime = sampleAnime.copy(notificationType = NotificationType.NEW_SEASONS)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(seasonsOnlyAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns emptyList()

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `passes today from injected clock when checking for new episodes`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(15)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(malId = 100, title = "Test", type = "TV", episodes = 25, sequels = emptyList())
        )

        useCase()

        coVerify { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) }
    }

    @Test
    fun `notifies when previously future episodes have now aired`() = runTest {
        val season = sampleSeason.copy(lastCheckedAiredEpisodeCount = 10)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(season)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(malId = 100, title = "Test", type = "TV", episodes = 25, sequels = emptyList())
        )

        val updates = useCase()

        val episodeUpdates = updates.filterIsInstance<AnimeUpdate.NewEpisodes>()
        assertThat(episodeUpdates).hasSize(1)
        assertThat(episodeUpdates[0].latestAiredEpisode).isEqualTo(12)
    }

    @Test
    fun `does not notify when last checked count matches aired count after date filtering`() = runTest {
        val season = sampleSeason.copy(lastCheckedAiredEpisodeCount = 12)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(season)
        coEvery { animeRepository.fetchLastAiredEpisodeNumber(100, fixedDate) } returns Result.success(12)
        coEvery { animeRepository.fetchAnimeFullById(100) } returns Result.success(
            AnimeFullDetails(malId = 100, title = "Test", type = "TV", episodes = 25, sequels = emptyList())
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).isEmpty()
    }
}
