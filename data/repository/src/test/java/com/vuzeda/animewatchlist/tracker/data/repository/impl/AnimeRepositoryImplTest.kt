package com.vuzeda.animewatchlist.tracker.data.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.NotificationType
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.TransactionRunner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AnimeRepositoryImplTest {

    private val animeDao: AnimeDao = mockk()
    private val seasonRepository: SeasonRepository = mockk(relaxed = true)
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val repository = AnimeRepositoryImpl(animeDao, seasonRepository, transactionRunner)

    private val sampleEntity = AnimeEntity(
        id = 1L,
        title = "Attack on Titan",
        imageUrl = "https://example.com/aot.jpg",
        synopsis = "Humanity fights titans.",
        genres = "Action,Drama",
        status = "WATCHING",
        userRating = 9,
        notificationType = "NONE",
        addedAt = 1000L
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
    fun `addAnime inserts anime and delegates seasons to seasonRepository`() = runTest {
        coEvery { animeDao.insert(any()) } returns 5L

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
    fun `updateAnime delegates to dao`() = runTest {
        coEvery { animeDao.update(any()) } returns Unit

        repository.updateAnime(Anime(id = 1L, title = "Test", status = WatchStatus.COMPLETED))

        coVerify { animeDao.update(any()) }
    }

    @Test
    fun `deleteAnime delegates to dao`() = runTest {
        coEvery { animeDao.deleteById(1L) } returns Unit

        repository.deleteAnime(1L)

        coVerify { animeDao.deleteById(1L) }
    }

    @Test
    fun `updateNotificationType delegates to dao`() = runTest {
        coEvery { animeDao.updateNotificationType(id = 1L, notificationType = "BOTH") } returns Unit

        repository.updateNotificationType(id = 1L, notificationType = NotificationType.BOTH)

        coVerify { animeDao.updateNotificationType(id = 1L, notificationType = "BOTH") }
    }

    @Test
    fun `getNotificationEnabledAnime returns mapped domain models`() = runTest {
        val notifiedEntity = sampleEntity.copy(notificationType = "BOTH")
        coEvery { animeDao.getNotificationEnabledAnime() } returns listOf(notifiedEntity)

        val result = repository.getNotificationEnabledAnime()

        assertThat(result).hasSize(1)
        assertThat(result[0].isNotificationsEnabled).isTrue()
    }

    @Test
    fun `deleteAllData delegates to animeDao deleteAll`() = runTest {
        coEvery { animeDao.deleteAll() } returns Unit

        repository.deleteAllData()

        coVerify(exactly = 1) { animeDao.deleteAll() }
    }
}
