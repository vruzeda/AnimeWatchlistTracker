package com.vuzeda.animewatchlist.tracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.vuzeda.animewatchlist.tracker.module.notification.android.NotificationHelper
import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AnimeWatchlistApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var schedulerRepository: SchedulerRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
        schedulerRepository.schedulePeriodicAnimeUpdate()
    }
}
