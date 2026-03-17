package com.vuzeda.animewatchlist.tracker.module.ui.notification

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate

interface AnimeUpdateNotifier {
    fun showUpdateNotification(update: AnimeUpdate)
}
