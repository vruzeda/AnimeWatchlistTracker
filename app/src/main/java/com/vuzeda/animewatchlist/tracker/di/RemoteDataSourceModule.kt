package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.AnimeRemoteDataSourceImpl
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataSourceModule {

    @Provides
    @Singleton
    fun provideAnimeRemoteDataSource(
        jikanApiService: JikanApiService,
        chiakiService: ChiakiService
    ): AnimeRemoteDataSource =
        AnimeRemoteDataSourceImpl(jikanApiService, chiakiService)
}
