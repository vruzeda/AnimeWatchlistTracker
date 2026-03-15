package com.vuzeda.animewatchlist.tracker.module.remotedatasource

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import java.time.LocalDate

interface AnimeRemoteDataSource {

    suspend fun searchAnime(query: String): Result<List<SearchResult>>

    suspend fun fetchAnimeFullById(malId: Int): Result<AnimeFullDetails>

    suspend fun fetchAnimeEpisodes(malId: Int, page: Int): Result<EpisodePage>

    suspend fun fetchLastAiredEpisodeNumber(malId: Int, today: LocalDate): Result<Int?>

    suspend fun fetchWatchOrder(malId: Int): Result<List<SeasonData>>

    suspend fun fetchSeasonAnime(year: Int, season: AnimeSeason, page: Int): Result<SeasonalAnimePage>
}
