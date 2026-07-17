package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.FakeChiakiService
import com.vuzeda.animewatchlist.tracker.FakeMalApiService
import com.vuzeda.animewatchlist.tracker.FakeMalEpisodeListService
import com.vuzeda.animewatchlist.tracker.FakeTenraiApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.TenraiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MockApiServiceModule {

    @Provides
    @Singleton
    fun provideFakeTenraiApiService(): FakeTenraiApiService = FakeTenraiApiService()

    @Provides
    @Singleton
    fun provideTenraiApiService(fakeTenraiApiService: FakeTenraiApiService): TenraiApiService =
        fakeTenraiApiService

    @Provides
    @Singleton
    fun provideMalApiService(fakeTenraiApiService: FakeTenraiApiService): MalApiService =
        FakeMalApiService(fakeTenraiApiService)

    @Provides
    @Singleton
    fun provideMalEpisodeListService(fakeTenraiApiService: FakeTenraiApiService): MalEpisodeListService =
        FakeMalEpisodeListService(fakeTenraiApiService)

    @Provides
    @Singleton
    fun provideChiakiService(): ChiakiService = FakeChiakiService()
}
