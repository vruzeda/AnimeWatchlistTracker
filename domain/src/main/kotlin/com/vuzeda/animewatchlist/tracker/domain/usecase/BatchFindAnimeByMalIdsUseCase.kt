package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import javax.inject.Inject

/** Batch-checks which MAL IDs are already in the watchlist, returning their anime IDs. */
class BatchFindAnimeByMalIdsUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(malIds: List<Int>): Set<Int> =
        seasonRepository.findAnimeIdsBySeasonMalIds(malIds).keys
}
