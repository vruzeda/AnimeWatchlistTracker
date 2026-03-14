package com.vuzeda.animewatchlist.tracker.module.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.localdatasource.Anime as LocalAnime
import com.vuzeda.animewatchlist.tracker.module.localdatasource.AnimeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
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
    private val seasonRepository: SeasonRepository = mockk(relaxed = true)
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val repository = AnimeRepositoryImpl(animeLocalDataSource, seasonRepository, transactionRunner)

    private val sampleLocalAnime = LocalAnime(
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
        every { animeLocalDataSource.observeAll() } returns flowOf(listOf(sampleLocalAnime))

        repository.observeAll().test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("Attack on Titan")
            assertThat(result[0].status).isEqualTo(WatchStatus.WATCHING)
            awaitComplete()
        }
    }

    @Test
    fun `observeByStatus passes correct status string to data source`() = runTest {
        every { animeLocalDataSource.observeByStatus("COMPLETED") } returns flowOf(listOf(sampleLocalAnime.copy(status = "COMPLETED")))

        repository.observeByStatus(WatchStatus.COMPLETED).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            awaitComplete()
        }
    }

    @Test
    fun `observeById emits mapped domain model`() = runTest {
        every { animeLocalDataSource.observeById(1L) } returns flowOf(sampleLocalAnime)

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
        coEvery { animeLocalDataSource.updateNotificationType(id = 1L, notificationType = "BOTH") } returns Unit

        repository.updateNotificationType(id = 1L, notificationType = NotificationType.BOTH)

        coVerify { animeLocalDataSource.updateNotificationType(id = 1L, notificationType = "BOTH") }
    }

    @Test
    fun `getNotificationEnabledAnime returns mapped domain models`() = runTest {
        val notifiedLocalAnime = sampleLocalAnime.copy(notificationType = "BOTH")
        coEvery { animeLocalDataSource.getNotificationEnabledAnime() } returns listOf(notifiedLocalAnime)

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
}
