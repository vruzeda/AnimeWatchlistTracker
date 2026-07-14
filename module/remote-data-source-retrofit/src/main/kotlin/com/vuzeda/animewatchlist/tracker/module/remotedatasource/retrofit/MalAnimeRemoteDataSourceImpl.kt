package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.SearchFilterState
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toAnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toApiValue
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toEpisodePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSearchResultPage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSeasonDataList
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper.toSeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListService
import java.time.LocalDate
import javax.inject.Inject

class MalAnimeRemoteDataSourceImpl @Inject constructor(
    private val malApiService: MalApiService,
    private val malEpisodeListService: MalEpisodeListService,
    private val chiakiService: ChiakiService
) : AnimeRemoteDataSource {

    override suspend fun searchAnime(
        query: String,
        filterState: SearchFilterState,
        page: Int
    ): Result<SearchResultPage> = safeApiCall {
        malApiService.searchAnime(
            query = query,
            offset = (page - 1) * MalApiService.PAGE_SIZE
        ).toSearchResultPage(currentPage = page)
    }

    override suspend fun fetchAnimeFullById(malId: Int): Result<AnimeFullDetails> = safeApiCall {
        malApiService.getAnimeById(malId).toAnimeFullDetails()
    }

    override suspend fun fetchAnimeEpisodes(malId: Int, page: Int): Result<EpisodePage> = safeApiCall {
        malEpisodeListService.fetchEpisodePage(malId = malId, page = page).toEpisodePage(currentPage = page)
    }

    override suspend fun fetchEpisodesAiredBetween(
        malId: Int,
        after: LocalDate,
        upTo: LocalDate,
        startingFromEpisode: Int?
    ): Result<List<EpisodeInfo>> = safeApiCall {
        collectEpisodesAiredBetween(
            after = after,
            upTo = upTo,
            startingFromEpisode = startingFromEpisode
        ) { page ->
            malEpisodeListService.fetchEpisodePage(malId = malId, page = page).toEpisodePage(currentPage = page)
        }
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
        malApiService.getSeasonAnime(
            year = year,
            season = season.toApiValue(),
            offset = (page - 1) * MalApiService.PAGE_SIZE
        ).toSeasonalAnimePage(currentPage = page)
    }
}
