package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Adds additional seasons to an existing anime in the watchlist. */
class AddSeasonsToAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(animeId: Long, seasons: List<Season>) =
        animeRepository.addSeasonsToAnime(animeId, seasons)
}
