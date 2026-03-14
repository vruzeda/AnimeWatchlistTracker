package com.vuzeda.animewatchlist.tracker.module.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
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

    @Test
    fun `observeAll emits mapped domain models`() = runTest {
        every { animeLocalDataSource.observeAll() } returns flowOf(listOf(sampleAnime))

        repository.observeAll().test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("Attack on Titan")
            assertThat(result[0].status).isEqualTo(WatchStatus.WATCHING)
            awaitComplete()
        }
    }

    @Test
    fun `observeByStatus passes correct status to data source`() = runTest {
        every { animeLocalDataSource.observeByStatus(WatchStatus.COMPLETED) } returns flowOf(listOf(sampleAnime.copy(status = WatchStatus.COMPLETED)))

        repository.observeByStatus(WatchStatus.COMPLETED).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            awaitComplete()
        }
    }

    @Test
    fun `observeById emits mapped domain model`() = runTest {
        every { animeLocalDataSource.observeById(1L) } returns flowOf(sampleAnime)

        repository.observeById(1L).test {
            val result = awaitItem()

            assertThat(result).isNotNull()
            assertThat(result?.title).isEqualTo("Attack on Titan")
            awaitComplete()
        }
    }

    @Test
    fun `observeById emits null when not found`() = runTest {
        every { animeLocalDataSource.observeById(999L) } returns flowOf(null)

        repository.observeById(999L).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `getAnimeById returns anime when found`() = runTest {
        coEvery { animeLocalDataSource.getById(1L) } returns sampleAnime

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
    fun `fetchLastAiredEpisodeNumber delegates to remote data source`() = runTest {
        val expected = Result.success(25)
        coEvery { animeRemoteDataSource.fetchLastAiredEpisodeNumber(100) } returns expected

        val result = repository.fetchLastAiredEpisodeNumber(100)

        assertThat(result).isEqualTo(expected)
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
