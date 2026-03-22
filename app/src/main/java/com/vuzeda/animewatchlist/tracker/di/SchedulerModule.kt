package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import com.vuzeda.animewatchlist.tracker.module.scheduler.work.AnimeUpdateWorkerScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {

    @Binds
    @Singleton
    abstract fun bindScheduler(impl: AnimeUpdateWorkerScheduler): AnimeUpdateScheduler
}
