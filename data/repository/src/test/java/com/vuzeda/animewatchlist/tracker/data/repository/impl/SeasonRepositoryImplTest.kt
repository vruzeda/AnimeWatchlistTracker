package com.vuzeda.animewatchlist.tracker.data.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.local.dao.SeasonDao
import com.vuzeda.animewatchlist.tracker.data.local.entity.SeasonEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.TransactionRunner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SeasonRepositoryImplTest {

    private val seasonDao: SeasonDao = mockk()
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val repository = SeasonRepositoryImpl(seasonDao, transactionRunner)

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
    fun `getSeasonsForAnime returns mapped season domain models`() = runTest {
        coEvery { seasonDao.getByAnimeId(1L) } returns listOf(sampleSeasonEntity)

        val result = repository.getSeasonsForAnime(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].malId).isEqualTo(16498)
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
    fun `updateSeason delegates to seasonDao`() = runTest {
        coEvery { seasonDao.update(any()) } returns Unit

        repository.updateSeason(Season(id = 1L, animeId = 1L, malId = 100, title = "S1", currentEpisode = 5))

        coVerify { seasonDao.update(any()) }
    }

    @Test
    fun `updateSeasonNotificationData delegates to seasonDao`() = runTest {
        coEvery { seasonDao.updateNotificationData(any(), any()) } returns Unit

        repository.updateSeasonNotificationData(seasonId = 1L, lastCheckedAiredEpisodeCount = 25)

        coVerify { seasonDao.updateNotificationData(seasonId = 1L, count = 25) }
    }
}
