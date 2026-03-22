package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import javax.inject.Inject

/** Records the current time as the last successful AnimeUpdateWorker run. */
class RecordAnimeUpdateRunUseCase @Inject constructor(
    private val schedulerRepository: SchedulerRepository
) {
    suspend operator fun invoke() = schedulerRepository.recordAnimeUpdateRun()
}
