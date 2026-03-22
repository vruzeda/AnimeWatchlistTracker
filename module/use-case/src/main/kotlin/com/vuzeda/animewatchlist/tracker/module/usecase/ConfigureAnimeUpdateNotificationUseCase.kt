package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Creates the notification channel and schedules the periodic anime update worker. */
class ConfigureAnimeUpdateNotificationUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    operator fun invoke() = animeRepository.configureAnimeUpdateNotification()
}
