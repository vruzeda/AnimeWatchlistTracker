package com.vuzeda.animewatchlist.tracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vuzeda.animewatchlist.tracker.module.usecase.ConfigureAnimeUpdateNotificationUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        FirebaseCrashlytics.getInstance()
            .setCrashlyticsCollectionEnabled(BuildConfig.FLAVOR != "mock" && !BuildConfig.DEBUG)
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseAuth.getInstance().signInAnonymously()
        }
        configureAnimeUpdateNotificationUseCase()
    }
}
