package com.vuzeda.animewatchlist.tracker.di

import android.content.Context
import com.vuzeda.animewatchlist.tracker.module.localdatasource.UserPreferencesLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.preferences.UserPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideUserPreferencesLocalDataSource(@ApplicationContext context: Context): UserPreferencesLocalDataSource =
        UserPreferencesDataStore(context)
}
