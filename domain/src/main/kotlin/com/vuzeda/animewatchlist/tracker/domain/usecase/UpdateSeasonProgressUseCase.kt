package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import javax.inject.Inject

/** Updates episode progress for a specific season. */
class UpdateSeasonProgressUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(season: Season, currentEpisode: Int) =
        seasonRepository.updateSeason(season.copy(currentEpisode = currentEpisode))
}
