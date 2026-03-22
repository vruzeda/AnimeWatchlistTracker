package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.MainActivity
import com.vuzeda.animewatchlist.tracker.module.notification.AnimeUpdateNotifier
import com.vuzeda.animewatchlist.tracker.module.notification.android.NotificationHelper
import com.vuzeda.animewatchlist.tracker.module.notification.android.NotificationLaunchActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    abstract fun bindAnimeUpdateNotifier(impl: NotificationHelper): AnimeUpdateNotifier

    companion object {

        @Provides
        @NotificationLaunchActivity
        fun provideNotificationLaunchActivity(): Class<*> = MainActivity::class.java
    }
}
