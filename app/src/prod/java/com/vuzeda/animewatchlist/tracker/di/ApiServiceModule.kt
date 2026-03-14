package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiServiceImpl
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
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
