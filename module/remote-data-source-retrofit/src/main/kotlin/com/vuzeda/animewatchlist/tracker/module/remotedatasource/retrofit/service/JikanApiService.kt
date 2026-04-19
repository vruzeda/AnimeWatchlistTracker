package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApiService {

    @GET("v4/anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("order_by") orderBy: String? = null,
        @Query("sort") sort: String? = null
    ): AnimeSearchResponseDto

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
        @Query("filter") filter: String? = null,
        @Query("continuing") continuing: Boolean = true
    ): AnimeSearchResponseDto

    companion object {
        const val BASE_URL = "https://api.jikan.moe/"
    }
}
