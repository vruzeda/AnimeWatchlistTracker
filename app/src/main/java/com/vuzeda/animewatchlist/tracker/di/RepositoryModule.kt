package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.AnimeRemoteDataSourceImpl
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import com.vuzeda.animewatchlist.tracker.module.repository.impl.AnimeRepositoryImpl
import com.vuzeda.animewatchlist.tracker.module.repository.impl.SchedulerRepositoryImpl
import com.vuzeda.animewatchlist.tracker.module.repository.impl.SeasonRepositoryImpl
import com.vuzeda.animewatchlist.tracker.module.repository.impl.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAnimeRepository(impl: AnimeRepositoryImpl): AnimeRepository

    @Binds
    @Singleton
    abstract fun bindSeasonRepository(impl: SeasonRepositoryImpl): SeasonRepository

    @Binds
    @Singleton
    abstract fun bindSchedulerRepository(impl: SchedulerRepositoryImpl): SchedulerRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
