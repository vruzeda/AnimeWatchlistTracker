package com.vuzeda.animewatchlist.tracker.module.notification

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate

interface AnimeUpdateNotifier {
    fun createNotificationChannel()
    fun showUpdateNotification(update: AnimeUpdate)
}
