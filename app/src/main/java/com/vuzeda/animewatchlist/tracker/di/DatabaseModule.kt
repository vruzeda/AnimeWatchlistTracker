package com.vuzeda.animewatchlist.tracker.di

import android.content.Context
import androidx.room.Room
import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.dao.SeasonDao
import com.vuzeda.animewatchlist.tracker.data.local.database.AnimeDatabase
import com.vuzeda.animewatchlist.tracker.data.local.database.RoomTransactionRunner
import com.vuzeda.animewatchlist.tracker.domain.repository.TransactionRunner
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
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideAnimeDao(database: AnimeDatabase): AnimeDao = database.animeDao()

    @Provides
    @Singleton
    fun provideSeasonDao(database: AnimeDatabase): SeasonDao = database.seasonDao()

    @Provides
    @Singleton
    fun provideTransactionRunner(database: AnimeDatabase): TransactionRunner =
        RoomTransactionRunner(database)
}
