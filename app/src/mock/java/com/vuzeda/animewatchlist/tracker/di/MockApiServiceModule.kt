package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.FakeChiakiService
import com.vuzeda.animewatchlist.tracker.FakeJikanApiService
import com.vuzeda.animewatchlist.tracker.data.api.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.data.api.service.JikanApiService
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
    fun provideJikanApiService(): JikanApiService = FakeJikanApiService()

    @Provides
    @Singleton
    fun provideChiakiService(): ChiakiService = FakeChiakiService()
}
