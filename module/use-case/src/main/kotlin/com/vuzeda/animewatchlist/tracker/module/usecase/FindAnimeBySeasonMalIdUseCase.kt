package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Checks if an anime is already in the watchlist by looking up a season's MAL ID. */
class FindAnimeBySeasonMalIdUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(malId: Int): Long? =
        seasonRepository.findAnimeIdBySeasonMalId(malId)
}
