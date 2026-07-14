package com.vuzeda.animewatchlist.tracker.module.repository.impl

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeProvider
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.SearchFilterState
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class ProviderSwitchingAnimeRemoteDataSource @Inject constructor(
    private val jikanDataSource: AnimeRemoteDataSource,
    private val malDataSource: AnimeRemoteDataSource,
    private val userPreferencesRepository: UserPreferencesRepository
) : AnimeRemoteDataSource {

    private suspend fun selectedDataSource(): AnimeRemoteDataSource =
        when (userPreferencesRepository.observeAnimeProvider().first()) {
            AnimeProvider.JIKAN -> jikanDataSource
            AnimeProvider.MAL -> malDataSource
        }

    override suspend fun searchAnime(
        query: String,
        filterState: SearchFilterState,
        page: Int
    ): Result<SearchResultPage> =
        selectedDataSource().searchAnime(query = query, filterState = filterState, page = page)

    override suspend fun fetchAnimeFullById(malId: Int): Result<AnimeFullDetails> =
        selectedDataSource().fetchAnimeFullById(malId)

    override suspend fun fetchAnimeEpisodes(malId: Int, page: Int): Result<EpisodePage> =
        selectedDataSource().fetchAnimeEpisodes(malId = malId, page = page)

    override suspend fun fetchEpisodesAiredBetween(
        malId: Int,
        after: LocalDate,
        upTo: LocalDate,
        startingFromEpisode: Int?
    ): Result<List<EpisodeInfo>> =
        selectedDataSource().fetchEpisodesAiredBetween(
            malId = malId,
            after = after,
            upTo = upTo,
            startingFromEpisode = startingFromEpisode
        )

    override suspend fun fetchWatchOrder(malId: Int): Result<List<SeasonData>> =
        selectedDataSource().fetchWatchOrder(malId)

    override suspend fun fetchSeasonAnime(
        year: Int,
        season: AnimeSeason,
        page: Int,
        filter: AnimeSearchType
    ): Result<SeasonalAnimePage> =
        selectedDataSource().fetchSeasonAnime(year = year, season = season, page = page, filter = filter)
}
