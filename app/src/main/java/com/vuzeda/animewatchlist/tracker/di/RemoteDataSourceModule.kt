package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.AnimeRemoteDataSourceImpl
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.MalAnimeRemoteDataSourceImpl
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListService
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import com.vuzeda.animewatchlist.tracker.module.repository.impl.ProviderSwitchingAnimeRemoteDataSource
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
        malApiService: MalApiService,
        malEpisodeListService: MalEpisodeListService,
        chiakiService: ChiakiService,
        userPreferencesRepository: UserPreferencesRepository
    ): AnimeRemoteDataSource =
        ProviderSwitchingAnimeRemoteDataSource(
            jikanDataSource = AnimeRemoteDataSourceImpl(jikanApiService, chiakiService),
            malDataSource = MalAnimeRemoteDataSourceImpl(malApiService, malEpisodeListService, chiakiService),
            userPreferencesRepository = userPreferencesRepository
        )
}
