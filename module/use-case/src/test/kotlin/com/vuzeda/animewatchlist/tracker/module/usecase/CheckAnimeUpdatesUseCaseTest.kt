package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.time.Clock
import kotlin.time.Instant

class CheckAnimeUpdatesUseCaseTest {

    private val animeRepository = mockk<AnimeRepository>(relaxed = true)
    private val seasonRepository = mockk<SeasonRepository>(relaxed = true)
    private val fixedDate = LocalDate.of(2026, 3, 15)
    private val yesterday = fixedDate.minusDays(1)
    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(
            fixedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
    }
    private val useCase = CheckAnimeUpdatesUseCase(animeRepository, seasonRepository, fixedClock)

    private val sampleAnime = Anime(
        id = 1,
        title = "Test Anime",
        status = WatchStatus.WATCHING,
        notificationType = NotificationType.BOTH,
        lastSeasonCheckDate = yesterday
    )

    private val sampleSeason = Season(
        id = 10L,
        animeId = 1L,
        malId = 100,
        title = "Test Anime Season 1",
        type = "TV",
        orderIndex = 0,
        lastCheckedAiredEpisodeCount = 12,
        lastEpisodeCheckDate = yesterday,
        isInWatchlist = true
    )

    private fun episodeInfo(number: Int, aired: String?) =
        EpisodeInfo(number = number, title = null, aired = aired, isFiller = false, isRecap = false)

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
    fun `detects new episodes when episodes aired since last check date`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(listOf(episodeInfo(13, "2026-03-15")))
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(emptyList())

        val updates = useCase()

        val episodeUpdates = updates.filterIsInstance<AnimeUpdate.NewEpisodes>()
        assertThat(episodeUpdates).hasSize(1)
        assertThat(episodeUpdates[0].newEpisodeCount).isEqualTo(1)
        assertThat(episodeUpdates[0].anime).isEqualTo(sampleAnime)
        assertThat(episodeUpdates[0].season).isEqualTo(sampleSeason)
    }

    @Test
    fun `does not notify when no new episodes have aired since last check`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(emptyList())

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).isEmpty()
    }

    @Test
    fun `does not notify on first episode check run when lastEpisodeCheckDate is null`() = runTest {
        val firstRunSeason = sampleSeason.copy(lastEpisodeCheckDate = null)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(firstRunSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, LocalDate.MIN, fixedDate, 12)
        } returns Result.success(listOf(episodeInfo(13, "2026-03-15")))
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(emptyList())

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).isEmpty()
    }

    @Test
    fun `initializes lastEpisodeCheckDate to last aired date on first run`() = runTest {
        val firstRunSeason = sampleSeason.copy(lastEpisodeCheckDate = null)
        val lastAiredDate = LocalDate.of(2026, 3, 10)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(firstRunSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, LocalDate.MIN, fixedDate, 12)
        } returns Result.success(listOf(episodeInfo(12, "2026-03-10"), episodeInfo(11, "2026-03-03")))
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(emptyList())

        useCase()

        coVerify { seasonRepository.updateLastEpisodeCheckDate(10L, lastAiredDate) }
    }

    @Test
    fun `initializes lastEpisodeCheckDate to fast past when no episodes have aired dates on first run`() = runTest {
        val firstRunSeason = sampleSeason.copy(lastEpisodeCheckDate = null)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(firstRunSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, LocalDate.MIN, fixedDate, 12)
        } returns Result.success(listOf(episodeInfo(1, null)))
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(emptyList())

        useCase()

        coVerify { seasonRepository.updateLastEpisodeCheckDate(10L, LocalDate.MIN) }
    }

    @Test
    fun `initializes lastEpisodeCheckDate to far past on first run when no episodes found`() = runTest {
        val firstRunSeason = sampleSeason.copy(lastEpisodeCheckDate = null)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(firstRunSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, LocalDate.MIN, fixedDate, 12)
        } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(emptyList())

        useCase()

        coVerify { seasonRepository.updateLastEpisodeCheckDate(10L, LocalDate.MIN) }
    }

    @Test
    fun `updates lastCheckedAiredEpisodeCount and lastEpisodeCheckDate after finding new episodes`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(listOf(episodeInfo(13, "2026-03-14"), episodeInfo(14, "2026-03-15")))
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(emptyList())

        useCase()

        coVerify {
            seasonRepository.updateSeasonNotificationData(seasonId = 10L, lastCheckedAiredEpisodeCount = 14)
        }
        coVerify { seasonRepository.updateLastEpisodeCheckDate(10L, LocalDate.of(2026, 3, 15)) }
    }

    @Test
    fun `does not update lastCheckedAiredEpisodeCount or lastEpisodeCheckDate when no episodes returned`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(emptyList())

        useCase()

        coVerify(exactly = 0) { seasonRepository.updateSeasonNotificationData(any(), any()) }
        coVerify(exactly = 0) { seasonRepository.updateLastEpisodeCheckDate(any(), any()) }
    }

    @Test
    fun `detects new season when Chiaki entry has startDate after lastSeasonCheckDate`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(
                SeasonData(malId = 100, title = "Season 1", type = "TV", startDate = LocalDate.of(2003, 10, 4)),
                SeasonData(malId = 300, title = "Season 2", type = "TV", startDate = fixedDate)
            )
        )

        val updates = useCase()

        val seasonUpdates = updates.filterIsInstance<AnimeUpdate.NewSeason>()
        assertThat(seasonUpdates).hasSize(1)
        assertThat(seasonUpdates[0].sequelMalId).isEqualTo(300)
        assertThat(seasonUpdates[0].sequelTitle).isEqualTo("Season 2")
    }

    @Test
    fun `does not notify for new season on first run and initializes check date to last known season start`() = runTest {
        val firstRunAnime = sampleAnime.copy(lastSeasonCheckDate = null)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(firstRunAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(
                SeasonData(malId = 100, title = "Season 1", type = "TV", startDate = LocalDate.of(2003, 10, 4)),
                SeasonData(malId = 300, title = "Season 2", type = "TV", startDate = fixedDate)
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).isEmpty()
        coVerify { animeRepository.updateLastSeasonCheckDate(1L, LocalDate.of(2003, 10, 4)) }
    }

    @Test
    fun `does not notify for new season when startDate is in the future`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(
                SeasonData(malId = 100, title = "Season 1", type = "TV", startDate = LocalDate.of(2003, 10, 4)),
                SeasonData(malId = 300, title = "Season 2", type = "TV", startDate = fixedDate.plusDays(1))
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).isEmpty()
    }

    @Test
    fun `does not notify for new season when startDate is not after lastSeasonCheckDate`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(
                SeasonData(malId = 100, title = "Season 1", type = "TV", startDate = LocalDate.of(2003, 10, 4)),
                SeasonData(malId = 300, title = "Season 2", type = "TV", startDate = yesterday)
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).isEmpty()
    }

    @Test
    fun `skips season entry already present in known malIds`() = runTest {
        val season2 = sampleSeason.copy(id = 11L, malId = 300, orderIndex = 1)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason, season2)
        coEvery { animeRepository.fetchEpisodesAiredBetween(any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(300) } returns Result.success(
            listOf(
                SeasonData(malId = 100, title = "Season 1", type = "TV", startDate = LocalDate.of(2003, 10, 4)),
                SeasonData(malId = 300, title = "Season 2", type = "TV", startDate = fixedDate)
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).isEmpty()
    }

    @Test
    fun `does not notify for new season when startDate is null in Chiaki entry`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(
                SeasonData(malId = 300, title = "Season 2", type = "TV", startDate = null)
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).isEmpty()
    }

    @Test
    fun `only checks episodes when notification type is NEW_EPISODES`() = runTest {
        val episodesOnlyAnime = sampleAnime.copy(notificationType = NotificationType.NEW_EPISODES)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(episodesOnlyAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(listOf(episodeInfo(13, "2026-03-15")))

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).hasSize(1)
        coVerify(exactly = 0) { animeRepository.fetchWatchOrder(any()) }
    }

    @Test
    fun `only checks new seasons when notification type is NEW_SEASONS`() = runTest {
        val seasonsOnlyAnime = sampleAnime.copy(notificationType = NotificationType.NEW_SEASONS)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(seasonsOnlyAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(
                SeasonData(malId = 300, title = "Season 2", type = "TV", startDate = fixedDate)
            )
        )

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).hasSize(1)
        coVerify(exactly = 0) { animeRepository.fetchEpisodesAiredBetween(any(), any(), any(), any()) }
    }

    @Test
    fun `initializes lastSeasonCheckDate to last known season start date on first run`() = runTest {
        val firstRunAnime = sampleAnime.copy(lastSeasonCheckDate = null)
        val lastKnownStartDate = LocalDate.of(2003, 10, 4)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(firstRunAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchEpisodesAiredBetween(any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(SeasonData(malId = 100, title = "Season 1", type = "TV", startDate = lastKnownStartDate))
        )

        useCase()

        coVerify { animeRepository.updateLastSeasonCheckDate(1L, lastKnownStartDate) }
    }

    @Test
    fun `initializes lastSeasonCheckDate to today when no known season has a start date on first run`() = runTest {
        val firstRunAnime = sampleAnime.copy(lastSeasonCheckDate = null)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(firstRunAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchEpisodesAiredBetween(any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(SeasonData(malId = 100, title = "Season 1", type = "TV", startDate = null))
        )

        useCase()

        coVerify { animeRepository.updateLastSeasonCheckDate(1L, fixedDate) }
    }

    @Test
    fun `updates lastSeasonCheckDate to new season start date when new season is found`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchEpisodesAiredBetween(any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(
                SeasonData(malId = 100, title = "Season 1", type = "TV", startDate = LocalDate.of(2003, 10, 4)),
                SeasonData(malId = 300, title = "Season 2", type = "TV", startDate = fixedDate)
            )
        )

        useCase()

        coVerify { animeRepository.updateLastSeasonCheckDate(1L, fixedDate) }
    }

    @Test
    fun `does not update lastSeasonCheckDate when no new season is found`() = runTest {
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery { animeRepository.fetchEpisodesAiredBetween(any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(100) } returns Result.success(
            listOf(SeasonData(malId = 100, title = "Season 1", type = "TV", startDate = LocalDate.of(2003, 10, 4)))
        )

        useCase()

        coVerify(exactly = 0) { animeRepository.updateLastSeasonCheckDate(any(), any()) }
    }

    @Test
    fun `checks per-season episode notifications for seasons not covered by anime-level check`() = runTest {
        val perSeasonSeason = Season(
            id = 20L, animeId = 2L, malId = 200, title = "Standalone",
            lastCheckedAiredEpisodeCount = 5, lastEpisodeCheckDate = yesterday, isInWatchlist = true
        )
        coEvery { animeRepository.getNotificationEnabledAnime() } returns emptyList()
        coEvery { seasonRepository.getSeasonsWithEpisodeNotifications() } returns listOf(perSeasonSeason)
        coEvery { animeRepository.getAnimeById(2L) } returns Anime(id = 2L, title = "Standalone Anime")
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(200, yesterday, fixedDate, 5)
        } returns Result.success(listOf(episodeInfo(6, "2026-03-15"), episodeInfo(7, "2026-03-15"), episodeInfo(8, "2026-03-15")))

        val updates = useCase()

        val episodeUpdates = updates.filterIsInstance<AnimeUpdate.NewEpisodes>()
        assertThat(episodeUpdates).hasSize(1)
        assertThat(episodeUpdates[0].newEpisodeCount).isEqualTo(3)
    }

    @Test
    fun `skips per-season check when season was already checked at anime level`() = runTest {
        val episodesOnlyAnime = sampleAnime.copy(notificationType = NotificationType.NEW_EPISODES)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(episodesOnlyAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)
        coEvery {
            animeRepository.fetchEpisodesAiredBetween(100, yesterday, fixedDate, 12)
        } returns Result.success(emptyList())
        coEvery { seasonRepository.getSeasonsWithEpisodeNotifications() } returns listOf(sampleSeason)

        val updates = useCase()

        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).isEmpty()
    }

    @Test
    fun `skips per-season notification when anime cannot be found`() = runTest {
        val perSeasonSeason = Season(
            id = 20L, animeId = 99L, malId = 200, title = "Unknown",
            lastEpisodeCheckDate = yesterday, isInWatchlist = true
        )
        coEvery { animeRepository.getNotificationEnabledAnime() } returns emptyList()
        coEvery { seasonRepository.getSeasonsWithEpisodeNotifications() } returns listOf(perSeasonSeason)
        coEvery { animeRepository.getAnimeById(99L) } returns null

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `uses last watchlisted season by orderIndex to fetch Chiaki watch order`() = runTest {
        val season1 = sampleSeason.copy(id = 10L, malId = 100, orderIndex = 0, isInWatchlist = true)
        val season2 = sampleSeason.copy(id = 11L, malId = 200, orderIndex = 1, isInWatchlist = true)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(sampleAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(season1, season2)
        coEvery { animeRepository.fetchEpisodesAiredBetween(any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { animeRepository.fetchWatchOrder(200) } returns Result.success(emptyList())

        useCase()

        coVerify { animeRepository.fetchWatchOrder(200) }
        coVerify(exactly = 0) { animeRepository.fetchWatchOrder(100) }
    }

    @Test
    fun `does not check new seasons when watchlisted seasons list is empty`() = runTest {
        val seasonsOnlyAnime = sampleAnime.copy(notificationType = NotificationType.NEW_SEASONS)
        val nonWatchlistedSeason = sampleSeason.copy(isInWatchlist = false)
        coEvery { animeRepository.getNotificationEnabledAnime() } returns listOf(seasonsOnlyAnime)
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(nonWatchlistedSeason)

        val updates = useCase()

        assertThat(updates).isEmpty()
        coVerify(exactly = 0) { animeRepository.fetchWatchOrder(any()) }
    }
}
