package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import javax.inject.Inject

/** Adds additional seasons to an existing anime in the watchlist. */
class AddSeasonsToAnimeUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(animeId: Long, seasons: List<Season>) =
        seasonRepository.addSeasonsToAnime(animeId, seasons)
}
