package com.vuzeda.animewatchlist.tracker.di

import android.content.Context
import androidx.room.Room
import com.vuzeda.animewatchlist.tracker.module.localdatasource.AnimeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.SeasonLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.AnimeDatabase
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.MIGRATION_6_7
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.MIGRATION_7_8
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.MIGRATION_8_9
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.MIGRATION_9_10
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.MIGRATION_10_11
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.RoomTransactionRunner
import com.vuzeda.animewatchlist.tracker.module.repository.TransactionRunner
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
            .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
            .build()

    @Provides
    @Singleton
    fun provideAnimeLocalDataSource(database: AnimeDatabase): AnimeLocalDataSource =
        database.animeDao()

    @Provides
    @Singleton
    fun provideSeasonLocalDataSource(database: AnimeDatabase): SeasonLocalDataSource =
        database.seasonDao()

    @Provides
    @Singleton
    fun provideTransactionRunner(database: AnimeDatabase): TransactionRunner =
        RoomTransactionRunner(database)
}
