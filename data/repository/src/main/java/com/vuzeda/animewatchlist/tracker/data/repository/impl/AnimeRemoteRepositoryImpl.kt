package com.vuzeda.animewatchlist.tracker.data.repository.impl

import com.vuzeda.animewatchlist.tracker.data.api.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.data.api.service.JikanApiService
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toAnimeFullDetails
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toEpisodePage
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toSearchResult
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toSeasonDataList
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toSeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeSeason
import com.vuzeda.animewatchlist.tracker.domain.model.EpisodePage
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonData
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import javax.inject.Inject

class AnimeRemoteRepositoryImpl @Inject constructor(
    private val jikanApiService: JikanApiService,
    private val chiakiService: ChiakiService
) : AnimeRemoteRepository {

    override suspend fun searchAnime(query: String): Result<List<SearchResult>> = runCatching {
        jikanApiService.searchAnime(query = query).data
            .map { it.toSearchResult() }
            .distinctBy { it.malId }
    }

    override suspend fun fetchAnimeFullById(malId: Int): Result<AnimeFullDetails> = runCatching {
        jikanApiService.getAnimeFullById(malId).data.toAnimeFullDetails()
    }

    override suspend fun fetchAnimeEpisodes(malId: Int, page: Int): Result<EpisodePage> = runCatching {
        jikanApiService.getAnimeEpisodes(malId = malId, page = page).toEpisodePage(currentPage = page)
    }

    override suspend fun fetchLastAiredEpisodeNumber(malId: Int): Result<Int?> = runCatching {
        val firstPage = jikanApiService.getAnimeEpisodes(malId = malId, page = 1)
        val lastPage = if (firstPage.pagination.lastVisiblePage > 1) {
            jikanApiService.getAnimeEpisodes(
                malId = malId,
                page = firstPage.pagination.lastVisiblePage
            )
        } else {
            firstPage
        }
        lastPage.data
            .filter { it.aired != null }
            .maxByOrNull { it.malId }
            ?.malId
    }

    override suspend fun fetchWatchOrder(malId: Int): Result<List<SeasonData>> = runCatching {
        chiakiService.fetchWatchOrder(malId).toSeasonDataList()
    }

    override suspend fun fetchSeasonAnime(
        year: Int,
        season: AnimeSeason,
        page: Int
    ): Result<SeasonalAnimePage> = runCatching {
        jikanApiService.getSeasonAnime(
            year = year,
            season = season.apiValue,
            page = page
        ).toSeasonalAnimePage(currentPage = page)
    }
}
