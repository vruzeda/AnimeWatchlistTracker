package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import javax.inject.Inject

/** Updates an existing season in the watchlist. */
class UpdateSeasonUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(season: Season) = seasonRepository.updateSeason(season)
}
