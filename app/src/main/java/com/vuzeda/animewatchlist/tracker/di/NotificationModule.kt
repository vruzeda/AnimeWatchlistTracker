package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.module.ui.notification.AnimeUpdateNotifier
import com.vuzeda.animewatchlist.tracker.notification.NotificationHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    abstract fun bindAnimeUpdateNotifier(impl: NotificationHelper): AnimeUpdateNotifier
}
