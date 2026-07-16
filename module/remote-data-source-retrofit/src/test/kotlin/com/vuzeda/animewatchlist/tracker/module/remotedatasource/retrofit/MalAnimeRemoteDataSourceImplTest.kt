package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.domain.SearchFilterState
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ChiakiWatchOrderEntryDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeListResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeNodeWrapperDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeListPageDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeRowDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalPagingDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListRequestException
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MalAnimeRemoteDataSourceImplTest {

    private val malApiService: MalApiService = mockk()
    private val malEpisodeListService: MalEpisodeListService = mockk()
    private val chiakiService: ChiakiService = mockk()
    private val dataSource = MalAnimeRemoteDataSourceImpl(malApiService, malEpisodeListService, chiakiService)

    private fun listResponse(vararg anime: MalAnimeDto, next: String? = null) =
        MalAnimeListResponseDto(
            data = anime.map { MalAnimeNodeWrapperDto(it) },
            paging = MalPagingDto(next = next)
        )

    @Test
    fun `searchAnime maps page number to MAL offset`() = runTest {
        coEvery { malApiService.searchAnime(query = "frieren", offset = 40) } returns
            listResponse(MalAnimeDto(id = 1, title = "Frieren"))

        val result = dataSource.searchAnime("frieren", page = 3).getOrThrow()

        assertThat(result.currentPage).isEqualTo(3)
        coVerify { malApiService.searchAnime(query = "frieren", offset = 40) }
    }

    @Test
    fun `searchAnime ignores filter state because MAL search has no filters`() = runTest {
        coEvery { malApiService.searchAnime(query = "frieren", offset = 0) } returns
            listResponse(MalAnimeDto(id = 1, title = "Frieren"), next = "next-url")

        val filterState = SearchFilterState(type = AnimeSearchType.MOVIE)
        val result = dataSource.searchAnime("frieren", filterState = filterState).getOrThrow()

        assertThat(result.results).hasSize(1)
        assertThat(result.hasNextPage).isTrue()
    }

    @Test
    fun `fetchAnimeFullById maps MAL details`() = runTest {
        coEvery { malApiService.getAnimeById(52991) } returns
            MalAnimeDto(id = 52991, title = "Sousou no Frieren", mediaType = "tv")

        val details = dataSource.fetchAnimeFullById(52991).getOrThrow()

        assertThat(details.malId).isEqualTo(52991)
        assertThat(details.type).isEqualTo("TV")
    }

    @Test
    fun `fetchAnimeEpisodes maps scraped page to episode page`() = runTest {
        coEvery { malEpisodeListService.fetchEpisodePage(malId = 52991, page = 2) } returns
            MalEpisodeListPageDto(
                episodes = listOf(MalEpisodeRowDto(number = 101, titleEnglish = "Title", titleRomaji = "Title", titleJapanese = "タイトル", airedIsoDate = "2024-01-05")),
                hasNextPage = true
            )

        val page = dataSource.fetchAnimeEpisodes(malId = 52991, page = 2).getOrThrow()

        assertThat(page.episodes).hasSize(1)
        assertThat(page.episodes[0].number).isEqualTo(101)
        assertThat(page.episodes[0].aired).isEqualTo("2024-01-05")
        assertThat(page.episodes[0].isFiller).isFalse()
        assertThat(page.hasNextPage).isTrue()
        assertThat(page.nextPage).isEqualTo(3)
    }

    @Test
    fun `fetchEpisodesAiredBetween collects episodes in window across pages`() = runTest {
        coEvery { malEpisodeListService.fetchEpisodePage(malId = 1, page = 1) } returns
            MalEpisodeListPageDto(
                episodes = listOf(
                    MalEpisodeRowDto(number = 1, titleEnglish = "Old", titleRomaji = "Old", titleJapanese = "古い", airedIsoDate = "2024-01-01"),
                    MalEpisodeRowDto(number = 2, titleEnglish = "In window", titleRomaji = "In window", titleJapanese = "ウィンドウ内", airedIsoDate = "2024-02-01")
                ),
                hasNextPage = true
            )
        coEvery { malEpisodeListService.fetchEpisodePage(malId = 1, page = 2) } returns
            MalEpisodeListPageDto(
                episodes = listOf(
                    MalEpisodeRowDto(number = 3, titleEnglish = "Also in window", titleRomaji = "Also in window", titleJapanese = "もウィンドウ内", airedIsoDate = "2024-02-15"),
                    MalEpisodeRowDto(number = 4, titleEnglish = "Too new", titleRomaji = "Too new", titleJapanese = "新しすぎ", airedIsoDate = "2024-05-01")
                ),
                hasNextPage = true
            )

        val episodes = dataSource.fetchEpisodesAiredBetween(
            malId = 1,
            after = LocalDate.parse("2024-01-15"),
            upTo = LocalDate.parse("2024-03-01"),
            startingFromEpisode = null
        ).getOrThrow()

        assertThat(episodes.map { it.number }).containsExactly(2, 3).inOrder()
        coVerify(exactly = 0) { malEpisodeListService.fetchEpisodePage(malId = 1, page = 3) }
    }

    @Test
    fun `fetchEpisodesAiredBetween starts from the page containing the starting episode`() = runTest {
        coEvery { malEpisodeListService.fetchEpisodePage(malId = 1, page = 3) } returns
            MalEpisodeListPageDto(
                episodes = listOf(MalEpisodeRowDto(number = 201, titleEnglish = "Ep 201", titleRomaji = "Ep 201", titleJapanese = "第201話", airedIsoDate = "2024-02-01")),
                hasNextPage = false
            )

        val episodes = dataSource.fetchEpisodesAiredBetween(
            malId = 1,
            after = LocalDate.parse("2024-01-15"),
            upTo = LocalDate.parse("2024-03-01"),
            startingFromEpisode = 201
        ).getOrThrow()

        assertThat(episodes.map { it.number }).containsExactly(201)
        coVerify(exactly = 0) { malEpisodeListService.fetchEpisodePage(malId = 1, page = 1) }
    }

    @Test
    fun `fetchWatchOrder delegates to Chiaki`() = runTest {
        coEvery { chiakiService.fetchWatchOrder(1) } returns listOf(
            ChiakiWatchOrderEntryDto(
                malId = 1,
                title = "Season 1",
                titleEnglish = null,
                typeCode = 1,
                episodeCount = 12,
                score = 8.0,
                imageUrl = null,
                isMainSeries = true,
                startDate = null,
                endDate = null
            )
        )

        val seasons = dataSource.fetchWatchOrder(1).getOrThrow()

        assertThat(seasons).hasSize(1)
        assertThat(seasons[0].malId).isEqualTo(1)
    }

    @Test
    fun `fetchSeasonAnime maps page to offset and season to api value`() = runTest {
        coEvery { malApiService.getSeasonAnime(year = 2023, season = "fall", offset = 20) } returns
            listResponse(MalAnimeDto(id = 52991, title = "Sousou no Frieren"))

        val page = dataSource.fetchSeasonAnime(year = 2023, season = AnimeSeason.FALL, page = 2).getOrThrow()

        assertThat(page.results).hasSize(1)
        assertThat(page.currentPage).isEqualTo(2)
        coVerify { malApiService.getSeasonAnime(year = 2023, season = "fall", offset = 20) }
    }

    @Test
    fun `episode scrape failure with 404 maps to NotFound`() = runTest {
        coEvery { malEpisodeListService.fetchEpisodePage(malId = 1, page = 1) } throws
            MalEpisodeListRequestException(malId = 1, statusCode = 404)

        val result = dataSource.fetchAnimeEpisodes(malId = 1, page = 1)

        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.NotFound::class.java)
    }

    @Test
    fun `episode scrape failure with server error maps to Network error`() = runTest {
        coEvery { malEpisodeListService.fetchEpisodePage(malId = 1, page = 1) } throws
            MalEpisodeListRequestException(malId = 1, statusCode = 500)

        val result = dataSource.fetchAnimeEpisodes(malId = 1, page = 1)

        assertThat(result.exceptionOrNull()).isInstanceOf(DataError.Network::class.java)
    }

    @Test
    fun `retryable episode scrape failure is retried until success`() = runTest {
        coEvery { malEpisodeListService.fetchEpisodePage(malId = 1, page = 1) } throws
            MalEpisodeListRequestException(malId = 1, statusCode = 503) andThen
            MalEpisodeListPageDto(episodes = emptyList(), hasNextPage = false)

        val result = dataSource.fetchAnimeEpisodes(malId = 1, page = 1)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 2) { malEpisodeListService.fetchEpisodePage(malId = 1, page = 1) }
    }
}