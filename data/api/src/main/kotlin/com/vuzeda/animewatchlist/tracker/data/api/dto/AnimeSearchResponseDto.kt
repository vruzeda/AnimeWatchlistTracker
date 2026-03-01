package com.vuzeda.animewatchlist.tracker.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AnimeSearchResponseDto(
    @param:Json(name = "data") val data: List<AnimeDataDto>
)

@JsonClass(generateAdapter = false)
data class AnimeDataDto(
    @param:Json(name = "mal_id") val malId: Int,
    @param:Json(name = "title") val title: String,
    @param:Json(name = "images") val images: AnimeImagesDto? = null,
    @param:Json(name = "synopsis") val synopsis: String? = null,
    @param:Json(name = "episodes") val episodes: Int? = null,
    @param:Json(name = "score") val score: Double? = null,
    @param:Json(name = "genres") val genres: List<GenreDto>? = null,
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "aired") val aired: AiredDto? = null
)

@JsonClass(generateAdapter = false)
data class AiredDto(
    @param:Json(name = "from") val from: String? = null
)

@JsonClass(generateAdapter = false)
data class AnimeImagesDto(
    @param:Json(name = "jpg") val jpg: ImageUrlDto? = null
)

@JsonClass(generateAdapter = false)
data class ImageUrlDto(
    @param:Json(name = "large_image_url") val largeImageUrl: String? = null,
    @param:Json(name = "image_url") val imageUrl: String? = null
)

@JsonClass(generateAdapter = false)
data class GenreDto(
    @param:Json(name = "name") val name: String
)
