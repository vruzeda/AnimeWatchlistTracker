package com.vuzeda.animewatchlist.tracker.data.api.service

import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeFullResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeSingleResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApiService {

    @GET("v4/anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): AnimeSearchResponseDto

    @GET("v4/anime/{id}")
    suspend fun getAnimeById(@Path("id") malId: Int): AnimeSingleResponseDto

    @GET("v4/anime/{id}/full")
    suspend fun getAnimeFullById(@Path("id") malId: Int): AnimeFullResponseDto

    @GET("v4/anime/{id}/episodes")
    suspend fun getAnimeEpisodes(
        @Path("id") malId: Int,
        @Query("page") page: Int = 1
    ): AnimeEpisodesResponseDto

    @GET("v4/seasons/{year}/{season}")
    suspend fun getSeasonAnime(
        @Path("year") year: Int,
        @Path("season") season: String,
        @Query("page") page: Int = 1,
        @Query("filter") filter: String = "tv"
    ): AnimeSearchResponseDto

    @GET("v4/seasons/now")
    suspend fun getSeasonNow(
        @Query("page") page: Int = 1,
        @Query("filter") filter: String = "tv"
    ): AnimeSearchResponseDto

    companion object {
        const val BASE_URL = "https://api.jikan.moe/"
    }
}
