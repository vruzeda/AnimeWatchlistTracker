package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes all season MAL IDs currently in the watchlist. */
class ObserveWatchlistMalIdsUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    operator fun invoke(): Flow<Set<Int>> = seasonRepository.observeAllSeasonMalIds()
}
