package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateSchedulerState
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the full anime update scheduler state: last successful run, last attempt, and result. */
class ObserveAnimeUpdateSchedulerStateUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    operator fun invoke(): Flow<AnimeUpdateSchedulerState> = animeRepository.observeAnimeUpdateSchedulerState()
}
