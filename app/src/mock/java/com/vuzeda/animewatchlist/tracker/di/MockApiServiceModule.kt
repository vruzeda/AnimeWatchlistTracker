package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.FakeChiakiService
import com.vuzeda.animewatchlist.tracker.FakeJikanApiService
import com.vuzeda.animewatchlist.tracker.FakeMalApiService
import com.vuzeda.animewatchlist.tracker.FakeMalEpisodeListService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListService
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
    fun provideFakeJikanApiService(): FakeJikanApiService = FakeJikanApiService()

    @Provides
    @Singleton
    fun provideJikanApiService(fakeJikanApiService: FakeJikanApiService): JikanApiService =
        fakeJikanApiService

    @Provides
    @Singleton
    fun provideMalApiService(fakeJikanApiService: FakeJikanApiService): MalApiService =
        FakeMalApiService(fakeJikanApiService)

    @Provides
    @Singleton
    fun provideMalEpisodeListService(fakeJikanApiService: FakeJikanApiService): MalEpisodeListService =
        FakeMalEpisodeListService(fakeJikanApiService)

    @Provides
    @Singleton
    fun provideChiakiService(): ChiakiService = FakeChiakiService()
}
