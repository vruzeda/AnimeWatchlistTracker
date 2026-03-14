package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Updates episode progress for a specific season. */
class UpdateSeasonProgressUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(season: Season, currentEpisode: Int) =
        seasonRepository.updateSeason(season.copy(currentEpisode = currentEpisode))
}
