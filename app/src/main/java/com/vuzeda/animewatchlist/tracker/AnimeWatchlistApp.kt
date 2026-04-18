package com.vuzeda.animewatchlist.tracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.vuzeda.animewatchlist.tracker.module.usecase.ConfigureAnimeUpdateNotificationUseCase
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class AnimeWatchlistApp : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var configureAnimeUpdateNotificationUseCase: ConfigureAnimeUpdateNotificationUseCase

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        chain.proceed(
                            chain.request().newBuilder()
                                .header("User-Agent", BROWSER_USER_AGENT)
                                .build()
                        )
                    }
                    .build()
            }
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
        configureAnimeUpdateNotificationUseCase()
    }

    companion object {
        private const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
