package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Instant

/** Observes the timestamp of the last successful AnimeUpdateWorker run. Emits null if never run. */
class ObserveLastAnimeUpdateRunUseCase @Inject constructor(
    private val schedulerRepository: SchedulerRepository
) {
    operator fun invoke(): Flow<Instant?> = schedulerRepository.observeLastAnimeUpdateRun()
}
