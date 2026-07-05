package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.notification.AnimeUpdateNotifier
import javax.inject.Inject

class ShowAnimeUpdateNotificationUseCase @Inject constructor(
    private val animeUpdateNotifier: AnimeUpdateNotifier,
) {
    suspend operator fun invoke(update: AnimeUpdate, titleLanguage: TitleLanguage) {
        animeUpdateNotifier.createNotificationChannels()
        animeUpdateNotifier.showUpdateNotification(
            update = update,
            titleLanguage = titleLanguage,
        )
    }
}
