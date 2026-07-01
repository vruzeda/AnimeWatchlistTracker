package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ChiakiWatchOrderEntryDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodesPaginationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.SearchPaginationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiRequestException
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeRemoteDataSourceImplTest {

    private val jikanApiService: JikanApiService = mockk()
    private val chiakiService: ChiakiService = mockk()
    private val repository = AnimeRemoteDataSourceImpl(jikanApiService, chiakiService)

    private fun httpException(
        code: Int,
        message: String = "HTTP $code",
        headers: Map<String, String> = emptyMap()
    ): HttpException {
        val builder = okhttp3.Response.Builder()
            .code(code)
            .message(message)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("https://api.jikan.moe/").build())
        headers.forEach { (name, value) -> builder.addHeader(name, value) }
        return HttpException(Response.error<Any>("".toResponseBody(null), builder.build()))
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

        assertThat(result.results).hasSize(2)
        assertThat(result.results[0].malId).isEqualTo(1)
        assertThat(result.results[1].malId).isEqualTo(2)
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

        assertThat(result.results).hasSize(3)
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

        assertThat(result.results).hasSize(1)
        assertThat(result.results[0].title).isEqualTo("Naruto Original")
    }

    @Test
    fun `searchAnime returns paginated page with hasNextPage and currentPage`() = runTest {
        val response = AnimeSearchResponseDto(
            pagination = SearchPaginationDto(hasNextPage = true, lastVisiblePage = 5),
            data = listOf(AnimeDataDto(malId = 1, title = "Naruto"))
        )
        coEvery { jikanApiService.searchAnime(query = "naruto", page = 2) } returns response

        val result = repository.searchAnime("naruto", page = 2).getOrThrow()

        assertThat(result.currentPage).isEqualTo(2)
        assertThat(result.hasNextPage).isTrue()
        assertThat(result.results).hasSize(1)
    }

    @Test
    fun `searchAnime returns failure wrapping DataError Network on IOException`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws IOException("Connection reset")

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.Network::class.java)
        coVerify(exactly = 1) { jikanApiService.searchAnime(any()) }
    }

    @Test
    fun `searchAnime returns failure wrapping DataError NotFound on HTTP 404`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(404, "Not Found")

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.NotFound::class.java)
        coVerify(exactly = 1) { jikanApiService.searchAnime(any()) }
    }

    @Test
    fun `searchAnime returns failure wrapping DataError RateLimited on HTTP 429`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(429)

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.RateLimited::class.java)
        coVerify(exactly = 3) { jikanApiService.searchAnime(any()) }
    }

    @Test
    fun `searchAnime populates retryAfterMs from Retry-After header on HTTP 429`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(429, headers = mapOf("Retry-After" to "30"))

        val result = repository.searchAnime("naruto")

        val error = result.exceptionOrNull() as DataError.RateLimited
        assertThat(error.retryAfterMs).isEqualTo(30_000L)
        coVerify(exactly = 3) { jikanApiService.searchAnime(any()) }
    }

    @Test
    fun `searchAnime sets retryAfterMs to null when Retry-After header is absent on HTTP 429`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(429)

        val result = repository.searchAnime("naruto")

        val error = result.exceptionOrNull() as DataError.RateLimited
        assertThat(error.retryAfterMs).isNull()
        coVerify(exactly = 3) { jikanApiService.searchAnime(any()) }
    }

    @Test
    fun `searchAnime sets retryAfterMs to null when Retry-After header is non-numeric on HTTP 429`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(429, headers = mapOf("Retry-After" to "Wed, 21 Oct 2015 07:28:00 GMT"))

        val result = repository.searchAnime("naruto")

        val error = result.exceptionOrNull() as DataError.RateLimited
        assertThat(error.retryAfterMs).isNull()
        coVerify(exactly = 3) { jikanApiService.searchAnime(any()) }
    }

    @Test
    fun `searchAnime returns failure wrapping DataError Network on other HTTP errors`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(500)

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.Network::class.java)
        coVerify(exactly = 1) { jikanApiService.searchAnime(any()) }
    }

    @Test
    fun `searchAnime retries on HTTP 503 and succeeds on second attempt`() = runTest {
        val response = AnimeSearchResponseDto(data = listOf(AnimeDataDto(malId = 1, title = "Naruto")))
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(503) andThen response

        val result = repository.searchAnime("naruto")

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 2) { jikanApiService.searchAnime(any()) }
    }

    @Test
    fun `searchAnime exhausts retries and fails with DataError Network on persistent HTTP 504`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(504)

        val result = repository.searchAnime("naruto")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.Network::class.java)
        coVerify(exactly = 3) { jikanApiService.searchAnime(any()) }
    }

    @Test
    fun `searchAnime retries on HTTP 429 honoring Retry-After header and succeeds on second attempt`() = runTest {
        val response = AnimeSearchResponseDto(data = listOf(AnimeDataDto(malId = 1, title = "Naruto")))
        coEvery { jikanApiService.searchAnime(any()) } throws
            httpException(429, headers = mapOf("Retry-After" to "2")) andThen response

        val result = repository.searchAnime("naruto")

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 2) { jikanApiService.searchAnime(any()) }
        assertThat(testScheduler.currentTime).isEqualTo(2_000L)
    }

    @Test
    fun `searchAnime exhausts retries waiting the full uncapped Retry-After on persistent HTTP 429`() = runTest {
        coEvery { jikanApiService.searchAnime(any()) } throws httpException(429, headers = mapOf("Retry-After" to "120"))

        val result = repository.searchAnime("naruto")

        val error = result.exceptionOrNull() as DataError.RateLimited
        assertThat(error.retryAfterMs).isEqualTo(120_000L)
        coVerify(exactly = 3) { jikanApiService.searchAnime(any()) }
        assertThat(testScheduler.currentTime).isEqualTo(240_000L)
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
    fun `fetchEpisodesAiredBetween returns episodes aired in range on single page`() = runTest {
        val after = LocalDate.of(2026, 3, 14)
        val upTo = LocalDate.of(2026, 3, 15)
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 12, aired = "2026-03-14"),
                EpisodeDto(malId = 13, aired = "2026-03-15"),
                EpisodeDto(malId = 14, aired = "2026-03-16")
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchEpisodesAiredBetween(100, after, upTo, null).getOrThrow()

        assertThat(result).hasSize(1)
        assertThat(result[0].number).isEqualTo(13)
    }

    @Test
    fun `fetchEpisodesAiredBetween stops pagination when null aired date encountered`() = runTest {
        val after = LocalDate.of(2026, 3, 1)
        val upTo = LocalDate.of(2026, 3, 15)
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 2, hasNextPage = true),
            data = listOf(
                EpisodeDto(malId = 10, aired = "2026-03-10"),
                EpisodeDto(malId = 11, aired = null)
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchEpisodesAiredBetween(100, after, upTo, null).getOrThrow()

        assertThat(result).hasSize(1)
        assertThat(result[0].number).isEqualTo(10)
        coVerify(exactly = 0) { jikanApiService.getAnimeEpisodes(malId = 100, page = 2) }
    }

    @Test
    fun `fetchEpisodesAiredBetween paginates when hasNextPage and no stop condition`() = runTest {
        val after = LocalDate.of(2026, 3, 1)
        val upTo = LocalDate.of(2026, 3, 15)
        val page1 = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 2, hasNextPage = true),
            data = listOf(EpisodeDto(malId = 10, aired = "2026-03-10"))
        )
        val page2 = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 2, hasNextPage = false),
            data = listOf(EpisodeDto(malId = 11, aired = "2026-03-12"))
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page1
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 2) } returns page2

        val result = repository.fetchEpisodesAiredBetween(100, after, upTo, null).getOrThrow()

        assertThat(result).hasSize(2)
        assertThat(result.map { it.number }).containsExactly(10, 11).inOrder()
    }

    @Test
    fun `fetchEpisodesAiredBetween starts from correct page based on startingFromEpisode`() = runTest {
        val after = LocalDate.of(2026, 3, 1)
        val upTo = LocalDate.of(2026, 3, 15)
        val page2 = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 2, hasNextPage = false),
            data = listOf(EpisodeDto(malId = 101, aired = "2026-03-10"))
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 2) } returns page2

        val result = repository.fetchEpisodesAiredBetween(100, after, upTo, startingFromEpisode = 100).getOrThrow()

        assertThat(result).hasSize(1)
        coVerify(exactly = 0) { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) }
    }

    @Test
    fun `fetchEpisodesAiredBetween returns empty list when all episodes aired before after date`() = runTest {
        val after = LocalDate.of(2026, 3, 14)
        val upTo = LocalDate.of(2026, 3, 15)
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 1, aired = "2026-03-01"),
                EpisodeDto(malId = 2, aired = "2026-03-13")
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchEpisodesAiredBetween(100, after, upTo, null).getOrThrow()

        assertThat(result).isEmpty()
    }

    @Test
    fun `fetchEpisodesAiredBetween includes episode aired exactly on upTo date`() = runTest {
        val after = LocalDate.of(2026, 3, 14)
        val upTo = LocalDate.of(2026, 3, 15)
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(EpisodeDto(malId = 5, aired = "2026-03-15"))
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchEpisodesAiredBetween(100, after, upTo, null).getOrThrow()

        assertThat(result).hasSize(1)
        assertThat(result[0].number).isEqualTo(5)
    }

    @Test
    fun `fetchEpisodesAiredBetween handles ISO 8601 datetime strings`() = runTest {
        val after = LocalDate.of(2023, 1, 1)
        val upTo = LocalDate.of(2026, 3, 15)
        val page = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 1, hasNextPage = false),
            data = listOf(
                EpisodeDto(malId = 1, aired = "2023-01-05T00:00:00+00:00"),
                EpisodeDto(malId = 2, aired = "2026-06-01T00:00:00+00:00")
            )
        )
        coEvery { jikanApiService.getAnimeEpisodes(malId = 100, page = 1) } returns page

        val result = repository.fetchEpisodesAiredBetween(100, after, upTo, null).getOrThrow()

        assertThat(result).hasSize(1)
        assertThat(result[0].number).isEqualTo(1)
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

    @Test
    fun `fetchAnimeFullById retries on transient HTTP 504 and returns success on second attempt`() = runTest {
        val response = AnimeFullResponseDto(
            data = AnimeFullDataDto(malId = 56735, title = "Oh Boy, Was I Wrong About Her", relations = null)
        )
        coEvery { jikanApiService.getAnimeFullById(56735) } throws httpException(504) andThen response

        val result = repository.fetchAnimeFullById(56735)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().title).isEqualTo("Oh Boy, Was I Wrong About Her")
        coVerify(exactly = 2) { jikanApiService.getAnimeFullById(56735) }
    }

    @Test
    fun `fetchWatchOrder retries on transient Chiaki 503 and succeeds on second attempt`() = runTest {
        val entry = ChiakiWatchOrderEntryDto(
            malId = 1,
            title = "Naruto",
            typeCode = 1,
            episodeCount = 220,
            score = 8.0,
            imageUrl = null
        )
        coEvery { chiakiService.fetchWatchOrder(1) } throws
            ChiakiRequestException(malId = 1, statusCode = 503) andThen listOf(entry)

        val result = repository.fetchWatchOrder(1)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(1)
        coVerify(exactly = 2) { chiakiService.fetchWatchOrder(1) }
    }

    @Test
    fun `fetchWatchOrder exhausts retries and fails with DataError Unknown on persistent Chiaki 503`() = runTest {
        coEvery { chiakiService.fetchWatchOrder(1) } throws ChiakiRequestException(malId = 1, statusCode = 503)

        val result = repository.fetchWatchOrder(1)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.Unknown::class.java)
        coVerify(exactly = 3) { chiakiService.fetchWatchOrder(1) }
    }

    @Test
    fun `fetchWatchOrder does not retry Chiaki failures with non-retryable status codes`() = runTest {
        coEvery { chiakiService.fetchWatchOrder(1) } throws ChiakiRequestException(malId = 1, statusCode = 404)

        val result = repository.fetchWatchOrder(1)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.Unknown::class.java)
        coVerify(exactly = 1) { chiakiService.fetchWatchOrder(1) }
    }
}
