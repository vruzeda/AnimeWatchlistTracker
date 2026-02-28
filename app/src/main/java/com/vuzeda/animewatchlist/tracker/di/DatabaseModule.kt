package com.vuzeda.animewatchlist.tracker.di

import android.content.Context
import androidx.room.Room
import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.database.AnimeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAnimeDatabase(@ApplicationContext context: Context): AnimeDatabase =
        Room.databaseBuilder(
            context,
            AnimeDatabase::class.java,
            "anime_watchlist.db"
        )
            .addMigrations(AnimeDatabase.MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun provideAnimeDao(database: AnimeDatabase): AnimeDao = database.animeDao()
}
