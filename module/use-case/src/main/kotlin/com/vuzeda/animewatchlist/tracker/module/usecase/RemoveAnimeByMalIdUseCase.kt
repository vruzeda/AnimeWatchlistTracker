package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Removes an anime from the watchlist by a season's MAL ID, returning all affected MAL IDs. */
class RemoveAnimeByMalIdUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(malId: Int): Set<Int> {
        val animeId = seasonRepository.findAnimeIdBySeasonMalId(malId) ?: return emptySet()
        val seasons = seasonRepository.getSeasonsForAnime(animeId)
        val allMalIds = seasons.map { it.malId }.toSet()
        animeRepository.deleteAnime(animeId)
        return allMalIds
    }
}
