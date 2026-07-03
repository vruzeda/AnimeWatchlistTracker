package com.vuzeda.animewatchlist.tracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.disk.DiskCache
import coil.ImageLoaderFactory
import com.vuzeda.animewatchlist.tracker.module.localdatasource.UserPreferencesLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.usecase.ConfigureAnimeUpdateNotificationUseCase
import com.vuzeda.animewatchlist.tracker.module.covercache.DiskCachePolicyInterceptor
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltAndroidApp
class AnimeWatchlistApp : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var configureAnimeUpdateNotificationUseCase: ConfigureAnimeUpdateNotificationUseCase

    @Inject
    lateinit var userPreferencesLocalDataSource: UserPreferencesLocalDataSource

    private val offlineCoverCachingEnabled = AtomicBoolean(true)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(filesDir.resolve("image_cache"))
                    .build()
            }
            .respectCacheHeaders(false)
            .components {
                add(DiskCachePolicyInterceptor(offlineCoverCachingEnabled))
            }
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
        appScope.launch {
            userPreferencesLocalDataSource.observeIsOfflineCoverCachingEnabled()
                .collect { enabled -> offlineCoverCachingEnabled.set(enabled) }
        }
    }

    companion object {
        private const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
