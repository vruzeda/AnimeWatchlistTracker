package com.vuzeda.animewatchlist.tracker.domain.repository

import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.EpisodePage
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonData

interface AnimeRemoteRepository {

    suspend fun searchAnime(query: String): Result<List<SearchResult>>

    suspend fun fetchAnimeFullById(malId: Int): Result<AnimeFullDetails>

    suspend fun fetchAnimeEpisodes(malId: Int, page: Int): Result<EpisodePage>

    suspend fun fetchLastAiredEpisodeNumber(malId: Int): Result<Int?>

    suspend fun fetchWatchOrder(malId: Int): Result<List<SeasonData>>
}
