package com.vuzeda.animewatchlist.tracker.di

import android.content.Context
import android.webkit.WebSettings
import com.squareup.moshi.Moshi
import com.vuzeda.animewatchlist.tracker.BuildConfig
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.interceptor.BrowserUserAgentInterceptor
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.interceptor.MalClientIdInterceptor
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.interceptor.RateLimitInterceptor
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiServiceImpl
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalApiService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListService
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListServiceImpl
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.TenraiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

    @Provides
    @Singleton
    fun provideChiakiService(okHttpClient: OkHttpClient): ChiakiService =
        ChiakiServiceImpl(okHttpClient)

    @Provides
    @Singleton
    fun provideTenraiApiService(okHttpClient: OkHttpClient, moshi: Moshi): TenraiApiService =
        Retrofit.Builder()
            .baseUrl(TenraiApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TenraiApiService::class.java)

    @Provides
    @Singleton
    fun provideMalApiService(okHttpClient: OkHttpClient, moshi: Moshi): MalApiService =
        Retrofit.Builder()
            .baseUrl(MalApiService.BASE_URL)
            .client(
                okHttpClient.newBuilder()
                    .addInterceptor(MalClientIdInterceptor(BuildConfig.MAL_CLIENT_ID))
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MalApiService::class.java)

    @Provides
    @Singleton
    fun provideMalEpisodeListService(okHttpClient: OkHttpClient): MalEpisodeListService =
        MalEpisodeListServiceImpl(okHttpClient)

    @Provides
    @Singleton
    @BrowserUserAgent
    fun provideBrowserUserAgent(@ApplicationContext context: Context): String =
        runCatching { WebSettings.getDefaultUserAgent(context) }
            .map(BrowserUserAgentInterceptor::sanitizeWebViewUserAgent)
            .getOrDefault(BrowserUserAgentInterceptor.FALLBACK_BROWSER_USER_AGENT)

    @Provides
    @Singleton
    fun provideOkHttpClient(@BrowserUserAgent userAgent: String): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(BrowserUserAgentInterceptor(userAgent))
            .addInterceptor(RateLimitInterceptor())
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .build()
}
