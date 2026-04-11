package com.vuzeda.animewatchlist.tracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.vuzeda.animewatchlist.tracker.module.usecase.ConfigureAnimeUpdateNotificationUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AnimeWatchlistApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var configureAnimeUpdateNotificationUseCase: ConfigureAnimeUpdateNotificationUseCase

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
        configureAnimeUpdateNotificationUseCase()
    }
}
