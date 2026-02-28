package com.vuzeda.animewatchlist.tracker.data.api.service

import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface JikanApiService {

    @GET("v4/anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): AnimeSearchResponseDto

    companion object {
        const val BASE_URL = "https://api.jikan.moe/"
    }
}
