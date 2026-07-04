package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.notification.AnimeUpdateNotifier
import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import javax.inject.Inject

/** Orchestrates notification channel creation and periodic anime update scheduling. */
class ConfigureAnimeUpdateNotificationUseCase @Inject constructor(
    private val animeUpdateNotifier: AnimeUpdateNotifier,
    private val animeUpdateScheduler: AnimeUpdateScheduler
) {
    operator fun invoke() {
        animeUpdateNotifier.createNotificationChannels()
        animeUpdateScheduler.schedulePeriodicUpdate()
    }
}
