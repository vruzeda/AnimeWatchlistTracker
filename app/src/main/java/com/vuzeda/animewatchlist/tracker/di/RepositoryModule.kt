package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.data.repository.impl.AnimeRemoteRepositoryImpl
import com.vuzeda.animewatchlist.tracker.data.repository.impl.AnimeRepositoryImpl
import com.vuzeda.animewatchlist.tracker.data.repository.impl.SeasonRepositoryImpl
import com.vuzeda.animewatchlist.tracker.data.repository.impl.UserPreferencesRepositoryImpl
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.UserPreferencesRepository
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
    abstract fun bindAnimeRemoteRepository(impl: AnimeRemoteRepositoryImpl): AnimeRemoteRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
