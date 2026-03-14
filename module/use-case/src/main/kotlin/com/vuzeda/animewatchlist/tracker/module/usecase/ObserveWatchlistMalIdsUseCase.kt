package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes all season MAL IDs currently in the watchlist. */
class ObserveWatchlistMalIdsUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    operator fun invoke(): Flow<Set<Int>> = seasonRepository.observeAllSeasonMalIds()
}
