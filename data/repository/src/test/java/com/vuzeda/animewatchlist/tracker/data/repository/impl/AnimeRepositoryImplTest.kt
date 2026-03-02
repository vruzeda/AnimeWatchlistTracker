package com.vuzeda.animewatchlist.tracker.data.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.dao.SeasonDao
import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.data.local.entity.SeasonEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.TransactionRunner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AnimeRepositoryImplTest {

    private val animeDao: AnimeDao = mockk()
    private val seasonDao: SeasonDao = mockk()
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val repository = AnimeRepositoryImpl(animeDao, seasonDao, transactionRunner)

    private val sampleEntity = AnimeEntity(
        id = 1L,
        title = "Attack on Titan",
        imageUrl = "https://example.com/aot.jpg",
        synopsis = "Humanity fights titans.",
        genres = "Action,Drama",
        status = "WATCHING",
        userRating = 9,
        isNotificationsEnabled = 0,
        addedAt = 1000L
    )

    private val sampleSeasonEntity = SeasonEntity(
        id = 1L,
        animeId = 1L,
        malId = 16498,
        title = "Attack on Titan",
        type = "TV",
        episodeCount = 25,
        currentEpisode = 12,
        score = 8.5,
        orderIndex = 0
    )

    @Test
    fun `observeAll emits mapped domain models`() = runTest {
        every { animeDao.observeAll() } returns flowOf(listOf(sampleEntity))

        repository.observeAll().test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("Attack on Titan")
            assertThat(result[0].status).isEqualTo(WatchStatus.WATCHING)
            awaitComplete()
        }
    }

    @Test
    fun `observeByStatus passes correct status string to dao`() = runTest {
        every { animeDao.observeByStatus("COMPLETED") } returns flowOf(listOf(sampleEntity.copy(status = "COMPLETED")))

        repository.observeByStatus(WatchStatus.COMPLETED).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            awaitComplete()
        }
    }

    @Test
    fun `observeById emits mapped domain model`() = runTest {
        every { animeDao.observeById(1L) } returns flowOf(sampleEntity)

        repository.observeById(1L).test {
            val result = awaitItem()

            assertThat(result).isNotNull()
            assertThat(result?.title).isEqualTo("Attack on Titan")
            awaitComplete()
        }
    }

    @Test
    fun `observeById emits null when not found`() = runTest {
        every { animeDao.observeById(999L) } returns flowOf(null)

        repository.observeById(999L).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `observeSeasonsForAnime emits mapped season domain models`() = runTest {
        every { seasonDao.observeByAnimeId(1L) } returns flowOf(listOf(sampleSeasonEntity))

        repository.observeSeasonsForAnime(1L).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].malId).isEqualTo(16498)
            assertThat(result[0].title).isEqualTo("Attack on Titan")
            awaitComplete()
        }
    }

    @Test
    fun `addAnime inserts anime and seasons and returns id`() = runTest {
        coEvery { animeDao.insert(any()) } returns 5L
        coEvery { seasonDao.insertAll(any()) } returns Unit

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

        val seasonSlot = slot<List<SeasonEntity>>()
        coVerify { seasonDao.insertAll(capture(seasonSlot)) }
        assertThat(seasonSlot.captured[0].animeId).isEqualTo(5L)
    }

    @Test
    fun `updateAnime delegates to dao`() = runTest {
        coEvery { animeDao.update(any()) } returns Unit

        repository.updateAnime(Anime(id = 1L, title = "Test", status = WatchStatus.COMPLETED))

        coVerify { animeDao.update(any()) }
    }

    @Test
    fun `updateSeason delegates to seasonDao`() = runTest {
        coEvery { seasonDao.update(any()) } returns Unit

        repository.updateSeason(Season(id = 1L, animeId = 1L, malId = 100, title = "S1", currentEpisode = 5))

        coVerify { seasonDao.update(any()) }
    }

    @Test
    fun `deleteAnime delegates to dao`() = runTest {
        coEvery { animeDao.deleteById(1L) } returns Unit

        repository.deleteAnime(1L)

        coVerify { animeDao.deleteById(1L) }
    }

    @Test
    fun `toggleNotifications delegates to dao with correct int value`() = runTest {
        coEvery { animeDao.updateNotificationsEnabled(id = 1L, enabled = 1) } returns Unit

        repository.toggleNotifications(id = 1L, enabled = true)

        coVerify { animeDao.updateNotificationsEnabled(id = 1L, enabled = 1) }
    }

    @Test
    fun `getNotificationEnabledAnime returns mapped domain models`() = runTest {
        val notifiedEntity = sampleEntity.copy(isNotificationsEnabled = 1)
        coEvery { animeDao.getNotificationEnabledAnime() } returns listOf(notifiedEntity)

        val result = repository.getNotificationEnabledAnime()

        assertThat(result).hasSize(1)
        assertThat(result[0].isNotificationsEnabled).isTrue()
    }

    @Test
    fun `getSeasonsForAnime returns mapped season domain models`() = runTest {
        coEvery { seasonDao.getByAnimeId(1L) } returns listOf(sampleSeasonEntity)

        val result = repository.getSeasonsForAnime(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].malId).isEqualTo(16498)
    }

    @Test
    fun `updateSeasonNotificationData delegates to seasonDao`() = runTest {
        coEvery { seasonDao.updateNotificationData(any(), any()) } returns Unit

        repository.updateSeasonNotificationData(seasonId = 1L, lastCheckedAiredEpisodeCount = 25)

        coVerify { seasonDao.updateNotificationData(seasonId = 1L, count = 25) }
    }

    @Test
    fun `addSeasonsToAnime inserts seasons with correct animeId`() = runTest {
        coEvery { seasonDao.insertAll(any()) } returns Unit

        val seasons = listOf(
            Season(malId = 200, title = "Season 2", orderIndex = 1)
        )

        repository.addSeasonsToAnime(animeId = 5L, seasons = seasons)

        val seasonSlot = slot<List<SeasonEntity>>()
        coVerify { seasonDao.insertAll(capture(seasonSlot)) }
        assertThat(seasonSlot.captured[0].animeId).isEqualTo(5L)
        assertThat(seasonSlot.captured[0].malId).isEqualTo(200)
    }

    @Test
    fun `observeSeasonById emits mapped domain model`() = runTest {
        every { seasonDao.observeById(1L) } returns flowOf(sampleSeasonEntity)

        repository.observeSeasonById(1L).test {
            val result = awaitItem()

            assertThat(result).isNotNull()
            assertThat(result?.malId).isEqualTo(16498)
            assertThat(result?.title).isEqualTo("Attack on Titan")
            awaitComplete()
        }
    }

    @Test
    fun `observeSeasonById emits null when not found`() = runTest {
        every { seasonDao.observeById(999L) } returns flowOf(null)

        repository.observeSeasonById(999L).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `findAnimeIdBySeasonMalId returns animeId when found`() = runTest {
        coEvery { seasonDao.findByMalId(16498) } returns sampleSeasonEntity

        val result = repository.findAnimeIdBySeasonMalId(16498)

        assertThat(result).isEqualTo(1L)
    }

    @Test
    fun `findAnimeIdBySeasonMalId returns null when not found`() = runTest {
        coEvery { seasonDao.findByMalId(99999) } returns null

        val result = repository.findAnimeIdBySeasonMalId(99999)

        assertThat(result).isNull()
    }

    @Test
    fun `observeByNotificationEnabled emits mapped domain models for enabled`() = runTest {
        val notifiedEntity = sampleEntity.copy(isNotificationsEnabled = 1)
        every { animeDao.observeByNotificationEnabled(1) } returns flowOf(listOf(notifiedEntity))

        repository.observeByNotificationEnabled(true).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].isNotificationsEnabled).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `observeByNotificationEnabled emits mapped domain models for disabled`() = runTest {
        every { animeDao.observeByNotificationEnabled(0) } returns flowOf(listOf(sampleEntity))

        repository.observeByNotificationEnabled(false).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].isNotificationsEnabled).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `deleteAllData delegates to animeDao deleteAll`() = runTest {
        coEvery { animeDao.deleteAll() } returns Unit

        repository.deleteAllData()

        coVerify(exactly = 1) { animeDao.deleteAll() }
    }
}
