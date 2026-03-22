package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import javax.inject.Inject

/** Immediately schedules a one-time anime update check. */
class TriggerAnimeUpdateUseCase @Inject constructor(
    private val schedulerRepository: SchedulerRepository
) {
    operator fun invoke() = schedulerRepository.scheduleImmediateAnimeUpdate()
}
