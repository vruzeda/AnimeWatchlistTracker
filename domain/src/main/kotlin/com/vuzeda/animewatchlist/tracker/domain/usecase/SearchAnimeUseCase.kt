package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import javax.inject.Inject

/** Searches for anime via the remote API. */
class SearchAnimeUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteRepository
) {

    suspend operator fun invoke(query: String): Result<List<SearchResult>> =
        remoteRepository.searchAnime(query)
}
