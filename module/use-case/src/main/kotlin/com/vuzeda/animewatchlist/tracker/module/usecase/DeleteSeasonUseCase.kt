package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Removes a single season from the watchlist; also removes the parent anime if it was the last season. */
class DeleteSeasonUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository,
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(season: Season) {
        val siblings = seasonRepository.getSeasonsForAnime(season.animeId)
        val watchlistSiblings = siblings.filter { it.isInWatchlist }
        if (watchlistSiblings.size <= 1) {
            animeRepository.deleteAnime(season.animeId)
        } else {
            seasonRepository.deleteSeason(season.id)
        }
    }
}
