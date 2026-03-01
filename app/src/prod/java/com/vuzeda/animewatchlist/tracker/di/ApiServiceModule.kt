package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.data.api.service.JikanApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

    @Provides
    @Singleton
    fun provideJikanApiService(retrofit: Retrofit): JikanApiService =
        retrofit.create(JikanApiService::class.java)
}
