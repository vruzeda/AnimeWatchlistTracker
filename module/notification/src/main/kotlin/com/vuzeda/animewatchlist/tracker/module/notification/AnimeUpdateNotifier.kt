package com.vuzeda.animewatchlist.tracker.module.notification

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage

interface AnimeUpdateNotifier {
    fun createNotificationChannels()
    fun showUpdateNotification(update: AnimeUpdate, titleLanguage: TitleLanguage)
}
