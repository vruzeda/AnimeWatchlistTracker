package com.vuzeda.animewatchlist.tracker.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.analytics.firebase.FirebaseAnalyticsTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideAnalyticsTracker(firebaseAnalytics: FirebaseAnalytics): AnalyticsTracker =
        FirebaseAnalyticsTracker(firebaseAnalytics)
}
