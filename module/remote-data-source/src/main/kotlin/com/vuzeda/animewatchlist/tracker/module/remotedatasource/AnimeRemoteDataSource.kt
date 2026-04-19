package com.vuzeda.animewatchlist.tracker.module.remotedatasource

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.SearchFilterState
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import java.time.LocalDate

interface AnimeRemoteDataSource {

    suspend fun searchAnime(
        query: String,
        filterState: SearchFilterState = SearchFilterState(),
        page: Int = 1
    ): Result<SearchResultPage>

    suspend fun fetchAnimeFullById(malId: Int): Result<AnimeFullDetails>

    suspend fun fetchAnimeEpisodes(malId: Int, page: Int): Result<EpisodePage>

    suspend fun fetchEpisodesAiredBetween(
        malId: Int,
        after: LocalDate,
        upTo: LocalDate,
        startingFromEpisode: Int?
    ): Result<List<EpisodeInfo>>

    suspend fun fetchWatchOrder(malId: Int): Result<List<SeasonData>>

    suspend fun fetchSeasonAnime(
        year: Int,
        season: AnimeSeason,
        page: Int,
        filter: AnimeSearchType = AnimeSearchType.ALL
    ): Result<SeasonalAnimePage>
}
