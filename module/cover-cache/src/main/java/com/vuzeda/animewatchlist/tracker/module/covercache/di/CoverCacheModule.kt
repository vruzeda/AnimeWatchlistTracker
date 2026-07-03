package com.vuzeda.animewatchlist.tracker.module.covercache.di

import com.vuzeda.animewatchlist.tracker.module.covercache.AppCoverCacheRepository
import com.vuzeda.animewatchlist.tracker.module.repository.CoverCacheRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoverCacheModule {
    @Binds
    @Singleton
    abstract fun bindCoverCacheRepository(impl: AppCoverCacheRepository): CoverCacheRepository
}
