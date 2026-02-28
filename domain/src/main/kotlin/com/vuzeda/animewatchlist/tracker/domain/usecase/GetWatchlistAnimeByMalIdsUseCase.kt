package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Retrieves watchlist anime matching the given MAL IDs. */
class GetWatchlistAnimeByMalIdsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(malIds: List<Int>): List<Anime> =
        animeRepository.getAnimeByMalIds(malIds)
}
