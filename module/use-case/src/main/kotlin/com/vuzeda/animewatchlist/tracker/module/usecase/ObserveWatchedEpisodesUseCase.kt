package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the set of watched episode numbers for a season. */
class ObserveWatchedEpisodesUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {
    operator fun invoke(seasonId: Long): Flow<Set<Int>> =
        seasonRepository.observeWatchedEpisodesForSeason(seasonId)
}
