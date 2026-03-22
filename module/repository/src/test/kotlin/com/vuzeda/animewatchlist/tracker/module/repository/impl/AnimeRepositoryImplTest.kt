package com.vuzeda.animewatchlist.tracker.module.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.localdatasource.AnimeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.module.repository.TransactionRunner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AnimeRepositoryImplTest {

    private val animeLocalDataSource: AnimeLocalDataSource = mockk()
    private val animeRemoteDataSource: AnimeRemoteDataSource = mockk(relaxed = true)
    private val seasonRepository: SeasonRepository = mockk(relaxed = true)
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val repository = AnimeRepositoryImpl(animeLocalDataSource, animeRemoteDataSource, seasonRepository, transactionRunner)

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
        every { seasonRepository.observeAllSeasons() } returns flowOf(listOf(sampleSeason, nonWatchlistSeason))

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
    fun `observeByStatus excludes anime whose most recent season has a different status`() = runTest {
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
        coEvery { animeLocalDataSource.updateNotificationType(id = 1L, notificationType = NotificationType.BOTH) } returns Unit

        repository.updateNotificationType(id = 1L, notificationType = NotificationType.BOTH)

        coVerify { animeLocalDataSource.updateNotificationType(id = 1L, notificationType = NotificationType.BOTH) }
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
        val expected = Result.success(listOf(SearchResult(malId = 1, title = "Naruto")))
        coEvery { animeRemoteDataSource.searchAnime("naruto") } returns expected

        val result = repository.searchAnime("naruto")

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `fetchAnimeFullById delegates to remote data source`() = runTest {
        val details = AnimeFullDetails(malId = 21, title = "One Punch Man", type = "TV", episodes = 12, sequels = emptyList())
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
        val expected = Result.success(listOf(EpisodeInfo(number = 13, title = null, aired = "2026-03-15", isFiller = false, isRecap = false)))
        coEvery { animeRemoteDataSource.fetchEpisodesAiredBetween(100, after, upTo, 12) } returns expected

        val result = repository.fetchEpisodesAiredBetween(100, after, upTo, 12)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `updateLastSeasonCheckDate delegates to local data source`() = runTest {
        val date = LocalDate.of(2026, 3, 15)
        coEvery { animeLocalDataSource.updateLastSeasonCheckDate(1L, date) } returns Unit

        repository.updateLastSeasonCheckDate(1L, date)

        coVerify { animeLocalDataSource.updateLastSeasonCheckDate(1L, date) }
    }

    @Test
    fun `fetchWatchOrder delegates to remote data source`() = runTest {
        val expected = Result.success(emptyList<com.vuzeda.animewatchlist.tracker.module.domain.SeasonData>())
        coEvery { animeRemoteDataSource.fetchWatchOrder(100) } returns expected

        val result = repository.fetchWatchOrder(100)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `fetchSeasonAnime delegates to remote data source`() = runTest {
        val page = SeasonalAnimePage(results = emptyList(), hasNextPage = false, currentPage = 1)
        val expected = Result.success(page)
        coEvery { animeRemoteDataSource.fetchSeasonAnime(year = 2026, season = AnimeSeason.WINTER, page = 1) } returns expected

        val result = repository.fetchSeasonAnime(year = 2026, season = AnimeSeason.WINTER, page = 1)

        assertThat(result).isEqualTo(expected)
    }
}
