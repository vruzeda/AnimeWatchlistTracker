package com.vuzeda.animewatchlist.tracker.module.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.localdatasource.AnimeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.EpisodeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.notification.AnimeUpdateNotifier
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.module.repository.TransactionRunner
import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.time.Clock
import kotlin.time.Instant

class AnimeRepositoryImplTest {

    private val animeLocalDataSource: AnimeLocalDataSource = mockk()
    private val episodeLocalDataSource: EpisodeLocalDataSource = mockk(relaxed = true)
    private val animeRemoteDataSource: AnimeRemoteDataSource = mockk(relaxed = true)
    private val animeUpdateNotifier: AnimeUpdateNotifier = mockk(relaxUnitFun = true)
    private val animeUpdateScheduler: AnimeUpdateScheduler = mockk(relaxUnitFun = true)
    private val seasonRepository: SeasonRepository = mockk(relaxed = true)
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val fixedInstant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    private val clock: Clock = mockk {
        every { now() } returns fixedInstant
    }
    private val repository = AnimeRepositoryImpl(
        animeLocalDataSource,
        episodeLocalDataSource,
        animeRemoteDataSource,
        animeUpdateNotifier,
        animeUpdateScheduler,
        seasonRepository,
        transactionRunner,
        clock
    )

    private val sampleAnime = Anime(
        id = 1L,
        title = "Attack on Titan",
        imageUrl = "https://example.com/aot.jpg",
        synopsis = "Humanity fights titans.",
        genres = listOf("Action", "Drama"),
        status = WatchStatus.WATCHING,
        userRating = 9,
        notificationType = NotificationType.NONE,
        addedAt = 1000L
    )

    private val sampleSeason = Season(
        id = 1L,
        animeId = 1L,
        malId = 16498,
        title = "Season 1",
        orderIndex = 0,
        status = WatchStatus.WATCHING,
        isInWatchlist = true
    )

    @Test
    fun `observeAll derives status from most recent season`() = runTest {
        every { animeLocalDataSource.observeAll() } returns flowOf(listOf(sampleAnime))
        every { seasonRepository.observeAllSeasons() } returns flowOf(listOf(sampleSeason))

        repository.observeAll().test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("Attack on Titan")
            assertThat(result[0].status).isEqualTo(WatchStatus.WATCHING)
            awaitComplete()
        }
    }

    @Test
    fun `observeAll defaults status to PLAN_TO_WATCH when anime has no seasons`() = runTest {
        every { animeLocalDataSource.observeAll() } returns flowOf(listOf(sampleAnime))
        every { seasonRepository.observeAllSeasons() } returns flowOf(emptyList())

        repository.observeAll().test {
            val result = awaitItem()

            assertThat(result[0].status).isEqualTo(WatchStatus.PLAN_TO_WATCH)
            awaitComplete()
        }
    }

    @Test
    fun `observeAll ignores non-watchlist seasons when deriving status`() = runTest {
        val nonWatchlistSeason = sampleSeason.copy(
            id = 2L, orderIndex = 1, status = WatchStatus.COMPLETED, isInWatchlist = false
        )
        every { animeLocalDataSource.observeAll() } returns flowOf(listOf(sampleAnime))
        every { seasonRepository.observeAllSeasons() } returns flowOf(
            listOf(
                sampleSeason,
                nonWatchlistSeason
            )
        )

        repository.observeAll().test {
            val result = awaitItem()

            assertThat(result[0].status).isEqualTo(WatchStatus.WATCHING)
            awaitComplete()
        }
    }

    @Test
    fun `observeByStatus filters by derived season status`() = runTest {
        val completedSeason = sampleSeason.copy(status = WatchStatus.COMPLETED)
        every { animeLocalDataSource.observeAll() } returns flowOf(listOf(sampleAnime))
        every { seasonRepository.observeAllSeasons() } returns flowOf(listOf(completedSeason))

        repository.observeByStatus(WatchStatus.COMPLETED).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].status).isEqualTo(WatchStatus.COMPLETED)
            awaitComplete()
        }
    }

    @Test
    fun `observeByStatus excludes anime whose most recent season has a different status`() =
        runTest {
            every { animeLocalDataSource.observeAll() } returns flowOf(listOf(sampleAnime))
            every { seasonRepository.observeAllSeasons() } returns flowOf(listOf(sampleSeason))

            repository.observeByStatus(WatchStatus.COMPLETED).test {
                val result = awaitItem()

                assertThat(result).isEmpty()
                awaitComplete()
            }
        }

    @Test
    fun `observeById derives status from most recent season`() = runTest {
        every { animeLocalDataSource.observeById(1L) } returns flowOf(sampleAnime)
        every { seasonRepository.observeSeasonsForAnime(1L) } returns flowOf(listOf(sampleSeason))

        repository.observeById(1L).test {
            val result = awaitItem()

            assertThat(result).isNotNull()
            assertThat(result?.title).isEqualTo("Attack on Titan")
            assertThat(result?.status).isEqualTo(WatchStatus.WATCHING)
            awaitComplete()
        }
    }

    @Test
    fun `observeById emits null when not found`() = runTest {
        every { animeLocalDataSource.observeById(999L) } returns flowOf(null)
        every { seasonRepository.observeSeasonsForAnime(999L) } returns flowOf(emptyList())

        repository.observeById(999L).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `getAnimeById derives status from most recent season`() = runTest {
        coEvery { animeLocalDataSource.getById(1L) } returns sampleAnime
        coEvery { seasonRepository.getSeasonsForAnime(1L) } returns listOf(sampleSeason)

        val result = repository.getAnimeById(1L)

        assertThat(result).isEqualTo(sampleAnime)
    }

    @Test
    fun `getAnimeById returns null when not found`() = runTest {
        coEvery { animeLocalDataSource.getById(999L) } returns null

        val result = repository.getAnimeById(999L)

        assertThat(result).isNull()
    }

    @Test
    fun `addAnime inserts anime and delegates seasons to seasonRepository`() = runTest {
        coEvery { animeLocalDataSource.insert(any()) } returns 5L

        val anime = Anime(
            title = "Attack on Titan",
            status = WatchStatus.WATCHING,
            genres = listOf("Action")
        )
        val seasons = listOf(
            Season(malId = 16498, title = "Season 1", orderIndex = 0)
        )

        val result = repository.addAnime(anime, seasons)

        assertThat(result).isEqualTo(5L)
        coVerify { seasonRepository.addSeasonsToAnime(5L, seasons) }
    }

    @Test
    fun `updateAnime delegates to data source`() = runTest {
        coEvery { animeLocalDataSource.update(any()) } returns Unit

        repository.updateAnime(Anime(id = 1L, title = "Test", status = WatchStatus.COMPLETED))

        coVerify { animeLocalDataSource.update(any()) }
    }

    @Test
    fun `deleteAnime delegates to data source`() = runTest {
        coEvery { animeLocalDataSource.deleteById(1L) } returns Unit

        repository.deleteAnime(1L)

        coVerify { animeLocalDataSource.deleteById(1L) }
    }

    @Test
    fun `updateNotificationType delegates to data source`() = runTest {
        coEvery {
            animeLocalDataSource.updateNotificationType(
                id = 1L,
                notificationType = NotificationType.BOTH
            )
        } returns Unit

        repository.updateNotificationType(id = 1L, notificationType = NotificationType.BOTH)

        coVerify {
            animeLocalDataSource.updateNotificationType(
                id = 1L,
                notificationType = NotificationType.BOTH
            )
        }
    }

    @Test
    fun `getNotificationEnabledAnime returns domain models`() = runTest {
        val notifiedAnime = sampleAnime.copy(notificationType = NotificationType.BOTH)
        coEvery { animeLocalDataSource.getNotificationEnabledAnime() } returns listOf(notifiedAnime)

        val result = repository.getNotificationEnabledAnime()

        assertThat(result).hasSize(1)
        assertThat(result[0].isNotificationsEnabled).isTrue()
    }

    @Test
    fun `deleteAllData delegates to data source deleteAll`() = runTest {
        coEvery { animeLocalDataSource.deleteAll() } returns Unit

        repository.deleteAllData()

        coVerify(exactly = 1) { animeLocalDataSource.deleteAll() }
    }

    @Test
    fun `searchAnime delegates to remote data source`() = runTest {
        val page = SearchResultPage(
            results = listOf(SearchResult(malId = 1, title = "Naruto")),
            hasNextPage = false,
            currentPage = 1
        )
        val expected = Result.success(page)
        coEvery { animeRemoteDataSource.searchAnime("naruto", page = 1) } returns expected

        val result = repository.searchAnime("naruto")

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `searchAnime passes page param to remote data source`() = runTest {
        val page2 = SearchResultPage(
            results = listOf(SearchResult(malId = 2, title = "Bleach")),
            hasNextPage = true,
            currentPage = 2
        )
        val expected = Result.success(page2)
        coEvery { animeRemoteDataSource.searchAnime("bleach", page = 2) } returns expected

        val result = repository.searchAnime("bleach", page = 2)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `fetchAnimeFullById delegates to remote data source`() = runTest {
        val details = AnimeFullDetails(
            malId = 21,
            title = "One Punch Man",
            type = "TV",
            episodes = 12,
            sequels = emptyList()
        )
        val expected = Result.success(details)
        coEvery { animeRemoteDataSource.fetchAnimeFullById(21) } returns expected

        val result = repository.fetchAnimeFullById(21)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `fetchAnimeEpisodes delegates to remote data source`() = runTest {
        val page = EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 2)
        val expected = Result.success(page)
        coEvery { animeRemoteDataSource.fetchAnimeEpisodes(malId = 100, page = 1) } returns expected

        val result = repository.fetchAnimeEpisodes(malId = 100, page = 1)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `fetchEpisodesAiredBetween delegates to remote data source`() = runTest {
        val after = LocalDate.of(2026, 3, 14)
        val upTo = LocalDate.of(2026, 3, 15)
        val expected = Result.success(
            listOf(
                EpisodeInfo(
                    number = 13,
                    title = null,
                    aired = "2026-03-15",
                    isFiller = false,
                    isRecap = false
                )
            )
        )
        coEvery {
            animeRemoteDataSource.fetchEpisodesAiredBetween(
                100,
                after,
                upTo,
                12
            )
        } returns expected

        val result = repository.fetchEpisodesAiredBetween(100, after, upTo, 12)

        assertThat(result).isEqualTo(expected)
        coVerify { episodeLocalDataSource.upsertEpisodes(100, expected.getOrThrow()) }
    }

    @Test
    fun `fetchEpisodesAiredBetween does not cache episodes when remote fails`() = runTest {
        val after = LocalDate.of(2026, 3, 14)
        val upTo = LocalDate.of(2026, 3, 15)
        coEvery {
            animeRemoteDataSource.fetchEpisodesAiredBetween(100, after, upTo, 12)
        } returns Result.failure(Exception())

        repository.fetchEpisodesAiredBetween(100, after, upTo, 12)

        coVerify(exactly = 0) { episodeLocalDataSource.upsertEpisodes(any(), any()) }
    }

    @Test
    fun `updateLatestKnownSeasonStartDate delegates to local data source`() = runTest {
        val date = LocalDate.of(2026, 3, 15)
        coEvery { animeLocalDataSource.updateLatestKnownSeasonStartDate(1L, date) } returns Unit

        repository.updateLatestKnownSeasonStartDate(1L, date)

        coVerify { animeLocalDataSource.updateLatestKnownSeasonStartDate(1L, date) }
    }

    @Test
    fun `updateLastSeasonCheckPerformedDate delegates to local data source`() = runTest {
        val date = LocalDate.of(2026, 3, 15)
        coEvery { animeLocalDataSource.updateLastSeasonCheckPerformedDate(1L, date) } returns Unit

        repository.updateLastSeasonCheckPerformedDate(1L, date)

        coVerify { animeLocalDataSource.updateLastSeasonCheckPerformedDate(1L, date) }
    }

    @Test
    fun `fetchWatchOrder delegates to remote data source`() = runTest {
        val seasons = listOf(SeasonData(malId = 100, title = "Attack on Titan", type = "TV"))
        val expected = Result.success(seasons)
        coEvery { animeRemoteDataSource.fetchWatchOrder(100) } returns expected
        coEvery { seasonRepository.findAnimeIdBySeasonMalId(100) } returns 1L

        val result = repository.fetchWatchOrder(100)

        assertThat(result).isEqualTo(expected)
        coVerify { seasonRepository.upsertSeasonsFromWatchOrder(1L, seasons) }
    }

    @Test
    fun `fetchWatchOrder does not upsert seasons when remote fails`() = runTest {
        coEvery { animeRemoteDataSource.fetchWatchOrder(100) } returns Result.failure(Exception())

        repository.fetchWatchOrder(100)

        coVerify(exactly = 0) { seasonRepository.upsertSeasonsFromWatchOrder(any(), any()) }
    }

    @Test
    fun `fetchWatchOrder does not upsert seasons when anime not found locally`() = runTest {
        val seasons = listOf(SeasonData(malId = 100, title = "Attack on Titan", type = "TV"))
        coEvery { animeRemoteDataSource.fetchWatchOrder(100) } returns Result.success(seasons)
        coEvery { seasonRepository.findAnimeIdBySeasonMalId(100) } returns null

        repository.fetchWatchOrder(100)

        coVerify(exactly = 0) { seasonRepository.upsertSeasonsFromWatchOrder(any(), any()) }
    }

    @Test
    fun `fetchSeasonAnime delegates to remote data source`() = runTest {
        val page = SeasonalAnimePage(results = emptyList(), hasNextPage = false, currentPage = 1)
        val expected = Result.success(page)
        coEvery {
            animeRemoteDataSource.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.WINTER,
                page = 1
            )
        } returns expected

        val result = repository.fetchSeasonAnime(year = 2026, season = AnimeSeason.WINTER, page = 1)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `schedulePeriodicAnimeUpdate delegates to Scheduler`() {
        repository.schedulePeriodicAnimeUpdate()

        verify(exactly = 1) { animeUpdateScheduler.schedulePeriodicUpdate() }
    }

    @Test
    fun `scheduleImmediateAnimeUpdate delegates to Scheduler`() {
        repository.scheduleImmediateAnimeUpdate()

        verify(exactly = 1) { animeUpdateScheduler.scheduleImmediateUpdate() }
    }

    @Test
    fun `observeLastAnimeUpdateRun maps null Long to null Instant`() = runTest {
        every { animeLocalDataSource.observeLastAnimeUpdateRun() } returns flowOf(null)

        repository.observeLastAnimeUpdateRun().test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `observeLastAnimeUpdateRun maps epochMillis to Instant`() = runTest {
        val epochMillis = 1_700_000_000_000L
        every { animeLocalDataSource.observeLastAnimeUpdateRun() } returns flowOf(epochMillis)

        repository.observeLastAnimeUpdateRun().test {
            assertThat(awaitItem()).isEqualTo(Instant.fromEpochMilliseconds(epochMillis))
            awaitComplete()
        }
    }

    @Test
    fun `recordAnimeUpdateRun records success attempt via local data source`() = runTest {
        coEvery { animeLocalDataSource.recordAnimeUpdateAttempt(any(), any()) } returns Unit

        repository.recordAnimeUpdateRun()

        coVerify(exactly = 1) {
            animeLocalDataSource.recordAnimeUpdateAttempt(fixedInstant.toEpochMilliseconds(), AnimeUpdateResult.Success)
        }
    }

    @Test
    fun `recordAnimeUpdateAttempt delegates to local data source with current clock time`() = runTest {
        coEvery { animeLocalDataSource.recordAnimeUpdateAttempt(any(), any()) } returns Unit

        repository.recordAnimeUpdateAttempt(AnimeUpdateResult.Failure("timeout"))

        coVerify(exactly = 1) {
            animeLocalDataSource.recordAnimeUpdateAttempt(
                fixedInstant.toEpochMilliseconds(),
                AnimeUpdateResult.Failure("timeout")
            )
        }
    }

    @Test
    fun `observeAnimeUpdateSchedulerState maps all fields from local data source`() = runTest {
        val lastRunMs = 1_700_000_000_000L
        val attemptMs = 1_700_000_001_000L
        every { animeLocalDataSource.observeLastAnimeUpdateRun() } returns flowOf(lastRunMs)
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptAt() } returns flowOf(attemptMs)
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptResult() } returns flowOf("FAILURE")
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptFailureReason() } returns flowOf("timeout")
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptRetryCount() } returns flowOf(null)

        repository.observeAnimeUpdateSchedulerState().test {
            val state = awaitItem()
            assertThat(state.lastSuccessfulRunAt).isEqualTo(Instant.fromEpochMilliseconds(lastRunMs))
            assertThat(state.lastAttemptAt).isEqualTo(Instant.fromEpochMilliseconds(attemptMs))
            assertThat(state.lastAttemptResult).isEqualTo(AnimeUpdateResult.Failure("timeout"))
            awaitComplete()
        }
    }

    @Test
    fun `observeAnimeUpdateSchedulerState maps null fields when never run`() = runTest {
        every { animeLocalDataSource.observeLastAnimeUpdateRun() } returns flowOf(null)
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptAt() } returns flowOf(null)
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptResult() } returns flowOf(null)
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptFailureReason() } returns flowOf(null)
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptRetryCount() } returns flowOf(null)

        repository.observeAnimeUpdateSchedulerState().test {
            val state = awaitItem()
            assertThat(state.lastSuccessfulRunAt).isNull()
            assertThat(state.lastAttemptAt).isNull()
            assertThat(state.lastAttemptResult).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `observeAnimeUpdateSchedulerState maps WillRetry result`() = runTest {
        every { animeLocalDataSource.observeLastAnimeUpdateRun() } returns flowOf(null)
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptAt() } returns flowOf(1_000_000L)
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptResult() } returns flowOf("WILL_RETRY")
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptFailureReason() } returns flowOf("Network error")
        every { animeLocalDataSource.observeLastAnimeUpdateAttemptRetryCount() } returns flowOf(3)

        repository.observeAnimeUpdateSchedulerState().test {
            val state = awaitItem()
            assertThat(state.lastAttemptResult).isEqualTo(AnimeUpdateResult.WillRetry(reason = "Network error", retryCount = 3))
            awaitComplete()
        }
    }
}
