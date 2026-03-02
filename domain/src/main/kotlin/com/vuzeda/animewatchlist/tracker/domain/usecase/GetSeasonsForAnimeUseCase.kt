package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import javax.inject.Inject

/** Retrieves the current list of seasons for an anime. */
class GetSeasonsForAnimeUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(animeId: Long): List<Season> =
        seasonRepository.getSeasonsForAnime(animeId)
}
