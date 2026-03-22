package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import com.vuzeda.animewatchlist.tracker.module.repository.impl.SchedulerRepositoryImpl
import com.vuzeda.animewatchlist.tracker.module.scheduler.Scheduler
import com.vuzeda.animewatchlist.tracker.module.scheduler.work.SchedulerImpl
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
    abstract fun bindScheduler(impl: SchedulerImpl): Scheduler

    @Binds
    @Singleton
    abstract fun bindSchedulerRepository(impl: SchedulerRepositoryImpl): SchedulerRepository
}
