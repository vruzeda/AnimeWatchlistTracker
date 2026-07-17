package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Emits watchlist seasons that have a known airing season, for use in the schedule screen. */
class ObserveScheduleUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    operator fun invoke(): Flow<List<Season>> =
        seasonRepository.observeAllSeasons().map { seasons ->
            seasons.filter { it.isInWatchlist && it.airingSeasonYear != null && it.airingSeasonName != null }
        }
}
