package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.SearchFilterState
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Searches for anime via the remote API. */
class SearchAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(
        query: String,
        filterState: SearchFilterState = SearchFilterState(),
        page: Int = 1
    ): Result<SearchResultPage> =
        animeRepository.searchAnime(query = query, filterState = filterState, page = page)
}
