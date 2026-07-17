package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
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
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.TenraiApiService
import java.time.LocalDate
import javax.inject.Inject

class AnimeRemoteDataSourceImpl @Inject constructor(
    private val tenraiApiService: TenraiApiService,
    private val chiakiService: ChiakiService
) : AnimeRemoteDataSource {

    override suspend fun searchAnime(
        query: String,
        filterState: SearchFilterState,
        page: Int
    ): Result<SearchResultPage> = safeApiCall {
        tenraiApiService.searchAnime(
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
        tenraiApiService.getAnimeFullById(malId).data.toAnimeFullDetails()
    }

    override suspend fun fetchAnimeEpisodes(malId: Int, page: Int): Result<EpisodePage> = safeApiCall {
        tenraiApiService.getAnimeEpisodes(malId = malId, page = page).toEpisodePage(currentPage = page)
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
            tenraiApiService.getAnimeEpisodes(malId = malId, page = page).toEpisodePage(currentPage = page)
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
        tenraiApiService.getSeasonAnime(
            year = year,
            season = season.toApiValue(),
            page = page,
            filter = filter.toApiValue()
        ).toSeasonalAnimePage(currentPage = page)
    }
}
