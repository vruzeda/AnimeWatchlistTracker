package com.vuzeda.animewatchlist.tracker.data.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeFullResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeRelationDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.GenreDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.RelatedEntryDto
import com.vuzeda.animewatchlist.tracker.data.api.service.JikanApiService
import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.IOException

class AnimeRepositoryImplTest {

    private val animeDao: AnimeDao = mockk()
    private val jikanApiService: JikanApiService = mockk()
    private val repository = AnimeRepositoryImpl(animeDao, jikanApiService)

    private val sampleEntity = AnimeEntity(
        id = 1L,
        malId = 21,
        title = "One Punch Man",
        imageUrl = "https://example.com/opm.jpg",
        synopsis = "A hero.",
        episodeCount = 12,
        currentEpisode = 6,
        score = 8.7,
        userRating = 9,
        status = "WATCHING",
        genres = "Action,Comedy"
    )

    @Test
    fun `observeWatchlist emits mapped domain models`() = runTest {
        every { animeDao.observeAll() } returns flowOf(listOf(sampleEntity))

        repository.observeWatchlist().test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("One Punch Man")
            assertThat(result[0].status).isEqualTo(WatchStatus.WATCHING)
            awaitComplete()
        }
    }

    @Test
    fun `observeWatchlistByStatus passes correct status string to dao`() = runTest {
        every { animeDao.observeByStatus("COMPLETED") } returns flowOf(listOf(sampleEntity.copy(status = "COMPLETED")))

        repository.observeWatchlistByStatus(WatchStatus.COMPLETED).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            awaitComplete()
        }
    }

    @Test
    fun `getAnimeById returns mapped domain model when found`() = runTest {
        coEvery { animeDao.getById(1L) } returns sampleEntity

        val result = repository.getAnimeById(1L)

        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("One Punch Man")
    }

    @Test
    fun `getAnimeById returns null when not found`() = runTest {
        coEvery { animeDao.getById(999L) } returns null

        val result = repository.getAnimeById(999L)

        assertThat(result).isNull()
    }

    @Test
    fun `addAnime inserts entity and returns id`() = runTest {
        coEvery { animeDao.insert(any()) } returns 1L

        val anime = Anime(
            title = "One Punch Man",
            status = WatchStatus.WATCHING,
            genres = listOf("Action", "Comedy")
        )

        val result = repository.addAnime(anime)

        assertThat(result).isEqualTo(1L)
        coVerify { animeDao.insert(any()) }
    }

    @Test
    fun `updateAnime delegates to dao`() = runTest {
        coEvery { animeDao.update(any()) } returns Unit

        val anime = Anime(
            id = 1L,
            title = "One Punch Man",
            status = WatchStatus.COMPLETED
        )

        repository.updateAnime(anime)

        coVerify { animeDao.update(any()) }
    }

    @Test
    fun `deleteAnime delegates to dao`() = runTest {
        coEvery { animeDao.deleteById(1L) } returns Unit

        repository.deleteAnime(1L)

        coVerify { animeDao.deleteById(1L) }
    }

    @Test
    fun `searchAnime returns success with mapped results`() = runTest {
        val apiResponse = AnimeSearchResponseDto(
            data = listOf(
                AnimeDataDto(
                    malId = 21,
                    title = "One Punch Man",
                    genres = listOf(GenreDto(name = "Action"))
                )
            )
        )
        coEvery { jikanApiService.searchAnime(query = "one punch") } returns apiResponse

        val result = repository.searchAnime("one punch")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).hasSize(1)
        assertThat(result.getOrNull()?.get(0)?.title).isEqualTo("One Punch Man")
        assertThat(result.getOrNull()?.get(0)?.status).isEqualTo(WatchStatus.PLAN_TO_WATCH)
    }

    @Test
    fun `searchAnime returns failure on network error`() = runTest {
        coEvery { jikanApiService.searchAnime(query = "test") } throws IOException("Network error")

        val result = repository.searchAnime("test")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `getNotifiedAnime returns mapped domain models`() = runTest {
        val notifiedEntity = sampleEntity.copy(isNotificationsEnabled = 1)
        coEvery { animeDao.getNotifiedAnime() } returns listOf(notifiedEntity)

        val result = repository.getNotifiedAnime()

        assertThat(result).hasSize(1)
        assertThat(result[0].isNotificationsEnabled).isTrue()
    }

    @Test
    fun `fetchAnimeFullDetails returns mapped full details on success`() = runTest {
        val apiResponse = AnimeFullResponseDto(
            data = AnimeFullDataDto(
                malId = 100,
                title = "Test",
                episodes = 24,
                relations = listOf(
                    AnimeRelationDto(
                        relation = "Sequel",
                        entry = listOf(RelatedEntryDto(malId = 200, type = "anime", name = "Season 2"))
                    )
                )
            )
        )
        coEvery { jikanApiService.getAnimeFullById(100) } returns apiResponse

        val result = repository.fetchAnimeFullDetails(100)

        assertThat(result.isSuccess).isTrue()
        val details = result.getOrNull()
        assertThat(details?.malId).isEqualTo(100)
        assertThat(details?.episodes).isEqualTo(24)
        assertThat(details?.sequels).hasSize(1)
        assertThat(details?.sequels?.get(0)?.malId).isEqualTo(200)
    }

    @Test
    fun `fetchAnimeFullDetails returns failure on network error`() = runTest {
        coEvery { jikanApiService.getAnimeFullById(100) } throws IOException("Network error")

        val result = repository.fetchAnimeFullDetails(100)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `toggleNotifications delegates to dao with correct int value`() = runTest {
        coEvery { animeDao.updateNotificationsEnabled(id = 1L, enabled = 1) } returns Unit

        repository.toggleNotifications(id = 1L, enabled = true)

        coVerify { animeDao.updateNotificationsEnabled(id = 1L, enabled = 1) }
    }

    @Test
    fun `updateNotificationData delegates to dao with serialized sequel ids`() = runTest {
        coEvery { animeDao.updateNotificationData(any(), any(), any()) } returns Unit

        repository.updateNotificationData(
            id = 1L,
            lastCheckedEpisodeCount = 24,
            knownSequelMalIds = listOf(200, 300)
        )

        coVerify {
            animeDao.updateNotificationData(
                id = 1L,
                count = 24,
                sequelIds = "200,300"
            )
        }
    }

    @Test
    fun `getAnimeByMalIds returns mapped domain models`() = runTest {
        val entities = listOf(
            sampleEntity.copy(id = 1L, malId = 21),
            sampleEntity.copy(id = 2L, malId = 30, title = "Naruto")
        )
        coEvery { animeDao.getByMalIds(listOf(21, 30)) } returns entities

        val result = repository.getAnimeByMalIds(listOf(21, 30))

        assertThat(result).hasSize(2)
        assertThat(result[0].malId).isEqualTo(21)
        assertThat(result[1].malId).isEqualTo(30)
        coVerify { animeDao.getByMalIds(listOf(21, 30)) }
    }

    @Test
    fun `getAnimeByMalIds returns empty list when no matches`() = runTest {
        coEvery { animeDao.getByMalIds(listOf(999)) } returns emptyList()

        val result = repository.getAnimeByMalIds(listOf(999))

        assertThat(result).isEmpty()
    }
}
