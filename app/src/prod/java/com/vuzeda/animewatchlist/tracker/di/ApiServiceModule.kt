package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.data.api.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.data.api.service.ChiakiServiceImpl
import com.vuzeda.animewatchlist.tracker.data.api.service.JikanApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

    @Provides
    @Singleton
    fun provideJikanApiService(retrofit: Retrofit): JikanApiService =
        retrofit.create(JikanApiService::class.java)

    @Provides
    @Singleton
    fun provideChiakiService(okHttpClient: OkHttpClient): ChiakiService =
        ChiakiServiceImpl(okHttpClient)
}
