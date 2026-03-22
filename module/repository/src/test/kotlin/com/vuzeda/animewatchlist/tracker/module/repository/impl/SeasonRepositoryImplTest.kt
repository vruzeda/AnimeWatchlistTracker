package com.vuzeda.animewatchlist.tracker.module.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.localdatasource.SeasonLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.TransactionRunner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SeasonRepositoryImplTest {

    private val seasonLocalDataSource: SeasonLocalDataSource = mockk()
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val repository = SeasonRepositoryImpl(seasonLocalDataSource, transactionRunner)

    private val sampleSeason = Season(
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
    fun `observeAllSeasons emits all seasons from data source`() = runTest {
        every { seasonLocalDataSource.observeAll() } returns flowOf(listOf(sampleSeason))

        repository.observeAllSeasons().test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].malId).isEqualTo(16498)
            awaitComplete()
        }
    }

    @Test
    fun `observeSeasonsForAnime emits mapped season domain models`() = runTest {
        every { seasonLocalDataSource.observeByAnimeId(1L) } returns flowOf(listOf(sampleSeason))

        repository.observeSeasonsForAnime(1L).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].malId).isEqualTo(16498)
            assertThat(result[0].title).isEqualTo("Attack on Titan")
            awaitComplete()
        }
    }

    @Test
    fun `observeSeasonById emits mapped domain model`() = runTest {
        every { seasonLocalDataSource.observeById(1L) } returns flowOf(sampleSeason)

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
        every { seasonLocalDataSource.observeById(999L) } returns flowOf(null)

        repository.observeSeasonById(999L).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `findAnimeIdBySeasonMalId returns animeId when found`() = runTest {
        coEvery { seasonLocalDataSource.findByMalId(16498) } returns sampleSeason

        val result = repository.findAnimeIdBySeasonMalId(16498)

        assertThat(result).isEqualTo(1L)
    }

    @Test
    fun `findAnimeIdBySeasonMalId returns null when not found`() = runTest {
        coEvery { seasonLocalDataSource.findByMalId(99999) } returns null

        val result = repository.findAnimeIdBySeasonMalId(99999)

        assertThat(result).isNull()
    }

    @Test
    fun `findSeasonIdByMalId returns season id when found`() = runTest {
        coEvery { seasonLocalDataSource.findByMalId(16498) } returns sampleSeason

        val result = repository.findSeasonIdByMalId(16498)

        assertThat(result).isEqualTo(1L)
    }

    @Test
    fun `findSeasonIdByMalId returns null when not found`() = runTest {
        coEvery { seasonLocalDataSource.findByMalId(99999) } returns null

        val result = repository.findSeasonIdByMalId(99999)

        assertThat(result).isNull()
    }

    @Test
    fun `getSeasonsForAnime returns mapped season domain models`() = runTest {
        coEvery { seasonLocalDataSource.getByAnimeId(1L) } returns listOf(sampleSeason)

        val result = repository.getSeasonsForAnime(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].malId).isEqualTo(16498)
    }

    @Test
    fun `addSeasonsToAnime inserts seasons with correct animeId`() = runTest {
        coEvery { seasonLocalDataSource.insertAll(any()) } returns Unit

        val seasons = listOf(
            Season(malId = 200, title = "Season 2", orderIndex = 1)
        )

        repository.addSeasonsToAnime(animeId = 5L, seasons = seasons)

        val seasonSlot = slot<List<Season>>()
        coVerify { seasonLocalDataSource.insertAll(capture(seasonSlot)) }
        assertThat(seasonSlot.captured[0].animeId).isEqualTo(5L)
        assertThat(seasonSlot.captured[0].malId).isEqualTo(200)
    }

    @Test
    fun `updateSeason delegates to data source`() = runTest {
        coEvery { seasonLocalDataSource.update(any()) } returns Unit

        repository.updateSeason(Season(id = 1L, animeId = 1L, malId = 100, title = "S1", currentEpisode = 5))

        coVerify { seasonLocalDataSource.update(any()) }
    }

    @Test
    fun `updateSeasonNotificationData delegates to data source`() = runTest {
        coEvery { seasonLocalDataSource.updateNotificationData(any(), any()) } returns Unit

        repository.updateSeasonNotificationData(seasonId = 1L, lastCheckedAiredEpisodeCount = 25)

        coVerify { seasonLocalDataSource.updateNotificationData(seasonId = 1L, count = 25) }
    }

    @Test
    fun `updateSeasonNotificationData passes null count to data source`() = runTest {
        coEvery { seasonLocalDataSource.updateNotificationData(any(), any()) } returns Unit

        repository.updateSeasonNotificationData(seasonId = 1L, lastCheckedAiredEpisodeCount = null)

        coVerify { seasonLocalDataSource.updateNotificationData(seasonId = 1L, count = null) }
    }

    @Test
    fun `toggleSeasonEpisodeNotifications enables notifications`() = runTest {
        coEvery { seasonLocalDataSource.updateEpisodeNotificationsEnabled(any(), any()) } returns Unit

        repository.toggleSeasonEpisodeNotifications(seasonId = 1L, enabled = true)

        coVerify { seasonLocalDataSource.updateEpisodeNotificationsEnabled(seasonId = 1L, enabled = true) }
    }

    @Test
    fun `toggleSeasonEpisodeNotifications disables notifications`() = runTest {
        coEvery { seasonLocalDataSource.updateEpisodeNotificationsEnabled(any(), any()) } returns Unit

        repository.toggleSeasonEpisodeNotifications(seasonId = 1L, enabled = false)

        coVerify { seasonLocalDataSource.updateEpisodeNotificationsEnabled(seasonId = 1L, enabled = false) }
    }

    @Test
    fun `getSeasonsWithEpisodeNotifications returns seasons from data source`() = runTest {
        val notificationSeason = sampleSeason.copy(isEpisodeNotificationsEnabled = true)
        coEvery { seasonLocalDataSource.getSeasonsWithEpisodeNotifications() } returns listOf(notificationSeason)

        val result = repository.getSeasonsWithEpisodeNotifications()

        assertThat(result).hasSize(1)
        assertThat(result[0].isEpisodeNotificationsEnabled).isTrue()
    }

    @Test
    fun `observeAllSeasonMalIds emits set of malIds`() = runTest {
        every { seasonLocalDataSource.observeAllMalIds() } returns flowOf(listOf(100, 200, 300))

        repository.observeAllSeasonMalIds().test {
            val result = awaitItem()
            assertThat(result).containsExactly(100, 200, 300)
            awaitComplete()
        }
    }

    @Test
    fun `updateLastEpisodeCheckDate delegates to data source`() = runTest {
        val date = LocalDate.of(2026, 3, 15)
        coEvery { seasonLocalDataSource.updateLastEpisodeCheckDate(1L, date) } returns Unit

        repository.updateLastEpisodeCheckDate(1L, date)

        coVerify { seasonLocalDataSource.updateLastEpisodeCheckDate(1L, date) }
    }
}
