package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.analytics.NoOpAnalyticsTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MockAnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsTracker(): AnalyticsTracker = NoOpAnalyticsTracker
}
