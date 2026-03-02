package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Removes an anime from the watchlist by a season's MAL ID, returning all affected MAL IDs. */
class RemoveAnimeByMalIdUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(malId: Int): Set<Int> {
        val animeId = animeRepository.findAnimeIdBySeasonMalId(malId) ?: return emptySet()
        val seasons = animeRepository.getSeasonsForAnime(animeId)
        val allMalIds = seasons.map { it.malId }.toSet()
        animeRepository.deleteAnime(animeId)
        return allMalIds
    }
}
