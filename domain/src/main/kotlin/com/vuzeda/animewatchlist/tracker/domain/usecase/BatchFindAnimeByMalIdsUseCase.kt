package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Batch-checks which MAL IDs are already in the watchlist, returning their anime IDs. */
class BatchFindAnimeByMalIdsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(malIds: List<Int>): Set<Int> =
        animeRepository.findAnimeIdsBySeasonMalIds(malIds).keys
}
