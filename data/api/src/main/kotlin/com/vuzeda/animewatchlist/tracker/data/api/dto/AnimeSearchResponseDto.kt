package com.vuzeda.animewatchlist.tracker.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AnimeSearchResponseDto(
    @Json(name = "data") val data: List<AnimeDataDto>
)

@JsonClass(generateAdapter = false)
data class AnimeDataDto(
    @Json(name = "mal_id") val malId: Int,
    @Json(name = "title") val title: String,
    @Json(name = "images") val images: AnimeImagesDto? = null,
    @Json(name = "synopsis") val synopsis: String? = null,
    @Json(name = "episodes") val episodes: Int? = null,
    @Json(name = "score") val score: Double? = null,
    @Json(name = "genres") val genres: List<GenreDto>? = null
)

@JsonClass(generateAdapter = false)
data class AnimeImagesDto(
    @Json(name = "jpg") val jpg: ImageUrlDto? = null
)

@JsonClass(generateAdapter = false)
data class ImageUrlDto(
    @Json(name = "large_image_url") val largeImageUrl: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null
)

@JsonClass(generateAdapter = false)
data class GenreDto(
    @Json(name = "name") val name: String
)
