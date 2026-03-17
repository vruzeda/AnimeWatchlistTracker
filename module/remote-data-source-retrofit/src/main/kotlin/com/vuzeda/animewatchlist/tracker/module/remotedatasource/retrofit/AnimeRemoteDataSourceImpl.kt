package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toAnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toEpisodePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toEpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSearchResult
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSeasonDataList
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

class AnimeRemoteDataSourceImpl @Inject constructor(
    private val jikanApiService: JikanApiService,
    private val chiakiService: ChiakiService
) : AnimeRemoteDataSource {

    override suspend fun searchAnime(query: String): Result<List<SearchResult>> = safeApiCall {
        jikanApiService.searchAnime(query = query).data
            .map { it.toSearchResult() }
            .distinctBy { it.malId }
    }

    override suspend fun fetchAnimeFullById(malId: Int): Result<AnimeFullDetails> = safeApiCall {
        jikanApiService.getAnimeFullById(malId).data.toAnimeFullDetails()
    }

    override suspend fun fetchAnimeEpisodes(malId: Int, page: Int): Result<EpisodePage> = safeApiCall {
        jikanApiService.getAnimeEpisodes(malId = malId, page = page).toEpisodePage(currentPage = page)
    }

    override suspend fun fetchEpisodesAiredBetween(
        malId: Int,
        after: LocalDate,
        upTo: LocalDate,
        startingFromEpisode: Int?
    ): Result<List<EpisodeInfo>> = safeApiCall {
        val startPage = maxOf(1, (startingFromEpisode ?: 0) / 100 + 1)
        val accumulated = mutableListOf<EpisodeInfo>()
        var page = startPage
        var stopPagination = false

        while (!stopPagination) {
            val response = jikanApiService.getAnimeEpisodes(malId = malId, page = page)

            for (episode in response.data) {
                val airedDate = parseAiredDate(episode.aired)
                if (airedDate == null || airedDate.isAfter(upTo)) {
                    stopPagination = true
                    break
                }
                if (airedDate.isAfter(after)) {
                    accumulated += episode.toEpisodeInfo()
                }
            }

            if (!stopPagination && response.pagination.hasNextPage) {
                page++
            } else {
                break
            }
        }

        accumulated
    }

    override suspend fun fetchWatchOrder(malId: Int): Result<List<SeasonData>> = safeApiCall {
        chiakiService.fetchWatchOrder(malId).toSeasonDataList()
    }

    override suspend fun fetchSeasonAnime(
        year: Int,
        season: AnimeSeason,
        page: Int
    ): Result<SeasonalAnimePage> = safeApiCall {
        jikanApiService.getSeasonAnime(
            year = year,
            season = season.apiValue,
            page = page
        ).toSeasonalAnimePage(currentPage = page)
    }
}

private fun parseAiredDate(aired: String?): LocalDate? {
    if (aired == null) return null
    return try {
        LocalDate.parse(aired.take(10))
    } catch (_: Exception) {
        null
    }
}

private inline fun <T> safeApiCall(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: IOException) {
        Result.failure(DataError.Network(throwable = e))
    } catch (e: HttpException) {
        Result.failure(mapHttpException(e) as Throwable)
    } catch (e: Exception) {
        Result.failure(DataError.Unknown(throwable = e))
    }

private fun mapHttpException(e: HttpException): DataError = when (e.code()) {
    404 -> DataError.NotFound(errorMessage = e.message())
    429 -> DataError.RateLimited()
    else -> DataError.Network(throwable = e)
}
