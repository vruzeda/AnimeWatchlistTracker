package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import javax.inject.Inject

/** Searches for anime via the remote API. */
class SearchAnimeUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteDataSource
) {

    suspend operator fun invoke(query: String): Result<List<SearchResult>> =
        remoteRepository.searchAnime(query)
}
