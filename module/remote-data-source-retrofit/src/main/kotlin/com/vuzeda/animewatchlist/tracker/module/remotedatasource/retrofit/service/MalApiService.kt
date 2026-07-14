package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MalApiService {

    @GET("v2/anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = PAGE_SIZE,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = true,
        @Query("fields") fields: String = LIST_FIELDS
    ): MalAnimeListResponseDto

    @GET("v2/anime/{id}")
    suspend fun getAnimeById(
        @Path("id") malId: Int,
        @Query("fields") fields: String = DETAILS_FIELDS
    ): MalAnimeDto

    @GET("v2/anime/season/{year}/{season}")
    suspend fun getSeasonAnime(
        @Path("year") year: Int,
        @Path("season") season: String,
        @Query("limit") limit: Int = PAGE_SIZE,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = true,
        @Query("fields") fields: String = LIST_FIELDS
    ): MalAnimeListResponseDto

    companion object {
        const val BASE_URL = "https://api.myanimelist.net/"
        const val PAGE_SIZE = 20
        const val LIST_FIELDS =
            "alternative_titles,main_picture,media_type,status,num_episodes,mean,synopsis,genres"
        const val DETAILS_FIELDS = "$LIST_FIELDS,broadcast,start_season,related_anime"
    }
}
