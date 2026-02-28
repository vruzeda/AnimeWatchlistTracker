package com.vuzeda.animewatchlist.tracker.data.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.GenreDto
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
}
