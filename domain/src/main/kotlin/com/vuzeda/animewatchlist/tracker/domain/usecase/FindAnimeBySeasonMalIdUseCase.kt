package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Checks if an anime is already in the watchlist by looking up a season's MAL ID. */
class FindAnimeBySeasonMalIdUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(malId: Int): Long? =
        animeRepository.findAnimeIdBySeasonMalId(malId)
}
