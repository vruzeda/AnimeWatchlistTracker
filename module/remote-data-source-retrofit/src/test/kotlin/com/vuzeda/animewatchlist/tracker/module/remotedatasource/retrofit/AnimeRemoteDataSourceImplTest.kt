package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodesPaginationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.SearchPaginationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate

class AnimeRemoteDataSourceImplTest {

    private val jikanApiService: JikanApiService = mockk()
    private val chiakiService: ChiakiService = mockk()
    private val repository = AnimeRemoteDataSourceImpl(jikanApiService, chiakiService)

    private fun httpException(code: Int, message: String = "HTTP $code"): HttpException {
        val rawResponse = okhttp3.Response.Builder()
            .code(code)
            .message(message)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("https://api.jikan.moe/").build())
            .build()
        return HttpException(Response.error<Any>("".toResponseBody(null), rawResponse))
    }

    @Test
    fun `searchAnime deduplicates results by malId`() = runTest {
        val duplicatedData = listOf(
            AnimeDataDto(malId = 1, title = "Naruto"),
            AnimeDataDto(malId = 2, title = "Bleach"),
            AnimeDataDto(malId = 1, title = "Naruto")
        )
        coEvery { jikanApiService.searchAnime(query = "naruto") } returns
            AnimeSearchResponseDto(data = duplicatedData)

        val result = repository.searchAnime("naruto").getOrThrow()

        assertThat(result).hasSize(2)
        assertThat(result[0].malId).isEqualTo(1)
        assertThat(result[1].malId).isEqualTo(2)
    }

    @Test
    fun `searchAnime returns all results when no duplicates`() = runTest {
        val uniqueData = listOf(
            AnimeDataDto(malId = 1, title = "Naruto"),
            AnimeDataDto(malId = 2, title = "Bleach"),
            AnimeDataDto(malId = 3, title = "One Piece")
        )
        coEvery { jikanApiService.searchAnime(query = "anime") } returns
            AnimeSearchResponseDto(data = uniqueData)

        val result = repository.searchAnime("anime").getOrThrow()

        assertThat(result).hasSize(3)
    }

    @Test
    fun `searchAnime keeps first occurrence when duplicates exist`() = runTest {
        val duplicatedData = listOf(
            AnimeDataDto(malId = 1, title = "Naruto Original"),
            AnimeDataDto(malId = 1, title = "Naruto Duplicate")
        )
        coEvery { jikanApiService.searchAnime(query = "naruto") } returns
            AnimeSearchResponseDto(data = duplicatedData)

        val result = repository.searchAnime("naruto").getOrThrow()

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Naruto Original")
    }

    @Test
    fun `searchAnime returns failure wrapping DataError Network on IOException`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws IOException("Connection reset")

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.Network::class.java)
    }

    @Test
    fun `searchAnime returns failure wrapping DataError NotFound on HTTP 404`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(404, "Not Found")

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.NotFound::class.java)
    }

    @Test
    fun `searchAnime returns failure wrapping DataError RateLimited on HTTP 429`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(429)

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.RateLimited::class.java)
    }

    @Test
    fun `searchAnime returns failure wrapping DataError Network on other HTTP errors`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(500)

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.Network::class.java)
    }

    @Test
    fun `searchAnime returns failure wrapping DataError Unknown on unexpected exceptions`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws RuntimeException("Unexpected")

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.Unknown::class.java)
    }

    @Test
    fun `fetchSeasonAnime returns paginated results`() = runTest {
        val response = AnimeSearchResponseDto(
            pagination = SearchPaginationDto(hasNextPage = true, lastVisiblePage = 3),
            data = listOf(
                AnimeDataDto(malId = 1, title = "Frieren"),
                AnimeDataDto(malId = 2, title = "Jujutsu Kaisen")
            )
        )
        coEvery {
            jikanApiService.getSeasonAnime(
                year = 2026,
                season = "winter",
                page = 1
            )
        } returns response

        val result = repository.fetchSeasonAnime(
            year = 2026,
            season = AnimeSeason.WINTER,
            page = 1
        ).getOrThrow()

        assertThat(result.results).hasSize(2)
        assertThat(result.hasNextPage).isTrue()
        assertThat(result.currentPage).isEqualTo(1)
        assertThat(result.results[0].title).isEqualTo("Frieren")
    }

    @Test
    fun `fetchLastAiredEpisodeNumber fetches single page when lastVisiblePage is 1`() = runTest {
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 1, aired = "2023-01-01"),
                EpisodeDto(malId = 2, aired = "2023-01-08")
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchLastAiredEpisodeNumber(100, LocalDate.of(2026, 3, 15)).getOrThrow()

        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `fetchLastAiredEpisodeNumber fetches last page when multiple pages exist`() = runTest {
        val firstPage = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 3, hasNextPage = true),
            data = listOf(EpisodeDto(malId = 1, aired = "2023-01-01"))
        )
        val lastPage = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 3, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 50, aired = "2023-06-01"),
                EpisodeDto(malId = 51, aired = "2023-06-08")
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns firstPage
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 3) } returns lastPage

        val result = repository.fetchLastAiredEpisodeNumber(100, LocalDate.of(2026, 3, 15)).getOrThrow()

        assertThat(result).isEqualTo(51)
    }

    @Test
    fun `fetchLastAiredEpisodeNumber returns null when no episodes have aired`() = runTest {
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 1, aired = null),
                EpisodeDto(malId = 2, aired = null)
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchLastAiredEpisodeNumber(100, LocalDate.of(2026, 3, 15)).getOrThrow()

        assertThat(result).isNull()
    }

    @Test
    fun `fetchLastAiredEpisodeNumber excludes episodes with future air dates`() = runTest {
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 10, aired = "2026-03-01"),
                EpisodeDto(malId = 11, aired = "2026-03-08"),
                EpisodeDto(malId = 12, aired = "2026-04-01"),
                EpisodeDto(malId = 13, aired = "2026-04-08")
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchLastAiredEpisodeNumber(100, LocalDate.of(2026, 3, 15)).getOrThrow()

        assertThat(result).isEqualTo(11)
    }

    @Test
    fun `fetchLastAiredEpisodeNumber returns null when all episodes have future air dates`() = runTest {
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 1, aired = "2026-04-01"),
                EpisodeDto(malId = 2, aired = "2026-04-08")
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchLastAiredEpisodeNumber(100, LocalDate.of(2026, 3, 15)).getOrThrow()

        assertThat(result).isNull()
    }

    @Test
    fun `fetchLastAiredEpisodeNumber counts episode aired exactly on today as aired`() = runTest {
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 5, aired = "2026-03-15"),
                EpisodeDto(malId = 6, aired = "2026-03-16")
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchLastAiredEpisodeNumber(100, LocalDate.of(2026, 3, 15)).getOrThrow()

        assertThat(result).isEqualTo(5)
    }

    @Test
    fun `fetchLastAiredEpisodeNumber handles ISO 8601 datetime strings with timezone offset`() = runTest {
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 1, aired = "2023-01-05T00:00:00+00:00"),
                EpisodeDto(malId = 2, aired = "2026-06-01T00:00:00+00:00")
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchLastAiredEpisodeNumber(100, LocalDate.of(2026, 3, 15)).getOrThrow()

        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `fetchAnimeFullById returns full details on success`() = runTest {
        val response = AnimeFullResponseDto(
            data = AnimeFullDataDto(malId = 21, title = "One Punch Man", relations = null)
        )
        coEvery { jikanApiService.getAnimeFullById(21) } returns response

        val result = repository.fetchAnimeFullById(21).getOrThrow()

        assertThat(result.malId).isEqualTo(21)
        assertThat(result.title).isEqualTo("One Punch Man")
    }
}
