package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.SearchFilterState
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toAnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toApiValue
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toEpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toEpisodePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSearchResult
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSearchResultPage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSeasonDataList
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiRequestException
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

class AnimeRemoteDataSourceImpl @Inject constructor(
    private val jikanApiService: JikanApiService,
    private val chiakiService: ChiakiService
) : AnimeRemoteDataSource {

    override suspend fun searchAnime(
        query: String,
        filterState: SearchFilterState,
        page: Int
    ): Result<SearchResultPage> = safeApiCall {
        jikanApiService.searchAnime(
            query = query,
            page = page,
            type = filterState.type.toApiValue(),
            status = filterState.status.toApiValue(),
            orderBy = filterState.orderBy.toApiValue(),
            sort = if (filterState.orderBy.toApiValue() != null) {
                if (filterState.isAscending) "asc" else "desc"
            } else {
                null
            }
        ).toSearchResultPage(currentPage = page)
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
        val startPage = maxOf(1, ((startingFromEpisode ?: 0) - 1) / 100 + 1)
        val accumulated = mutableListOf<EpisodeInfo>()
        var page = startPage
        var stopPagination = false

        while (!stopPagination) {
            val response = jikanApiService.getAnimeEpisodes(malId = malId, page = page)

            var pageExceedsUpTo = false
            for (episode in response.data) {
                val airedDate = parseAiredDateFromString(episode.aired)
                if (airedDate != null) {
                    if (airedDate.isAfter(upTo)) {
                        pageExceedsUpTo = true
                        stopPagination = true
                        break
                    }
                    if (airedDate.isAfter(after)) {
                        accumulated += episode.toEpisodeInfo()
                    }
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
        page: Int,
        filter: AnimeSearchType
    ): Result<SeasonalAnimePage> = safeApiCall {
        jikanApiService.getSeasonAnime(
            year = year,
            season = season.toApiValue(),
            page = page,
            filter = filter.toApiValue()
        ).toSeasonalAnimePage(currentPage = page)
    }
}

private const val BASE_RETRY_DELAY_MS = 500L
private const val MAX_RETRY_ATTEMPTS = 2
private val RETRYABLE_HTTP_CODES = setOf(429, 502, 503, 504)

private suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    var attempt = 0
    while (true) {
        try {
            return Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            return Result.failure(DataError.Network(throwable = e))
        } catch (e: HttpException) {
            if (e.code() !in RETRYABLE_HTTP_CODES || attempt >= MAX_RETRY_ATTEMPTS) {
                return Result.failure(mapHttpException(e) as Throwable)
            }
            delay(e.retryAfterMs() ?: (BASE_RETRY_DELAY_MS * (attempt + 1)))
            attempt++
        } catch (e: ChiakiRequestException) {
            if (e.statusCode !in RETRYABLE_HTTP_CODES || attempt >= MAX_RETRY_ATTEMPTS) {
                return Result.failure(mapChiakiException(e) as Throwable)
            }
            delay(BASE_RETRY_DELAY_MS * (attempt + 1))
            attempt++
        } catch (e: Exception) {
            return Result.failure(DataError.Unknown(throwable = e))
        }
    }
}

private fun mapHttpException(e: HttpException): DataError = when (e.code()) {
    404 -> DataError.NotFound(errorMessage = e.message())
    429 -> DataError.RateLimited(retryAfterMs = e.retryAfterMs())
    else -> DataError.Network(throwable = e)
}

private fun mapChiakiException(e: ChiakiRequestException): DataError = when (e.statusCode) {
    404 -> DataError.NotFound(errorMessage = e.message ?: "Not found")
    429 -> DataError.RateLimited(retryAfterMs = null)
    else -> DataError.Network(throwable = e)
}

private fun HttpException.retryAfterMs(): Long? =
    response()?.headers()?.get("Retry-After")?.toLongOrNull()?.let { it * 1_000L }
