package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Marks an existing non-watchlist season as in-watchlist with the given status. */
class AddSeasonToWatchlistUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(season: Season, status: WatchStatus) =
        seasonRepository.updateSeason(season.copy(isInWatchlist = true, status = status))
}
