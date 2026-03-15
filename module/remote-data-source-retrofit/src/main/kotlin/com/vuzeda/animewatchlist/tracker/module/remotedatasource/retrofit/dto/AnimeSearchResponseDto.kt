package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchPaginationDto(
    @param:Json(name = "has_next_page") val hasNextPage: Boolean = false,
    @param:Json(name = "last_visible_page") val lastVisiblePage: Int = 1
)

@JsonClass(generateAdapter = true)
data class AnimeSearchResponseDto(
    @param:Json(name = "pagination") val pagination: SearchPaginationDto? = null,
    @param:Json(name = "data") val data: List<AnimeDataDto>
)

@JsonClass(generateAdapter = true)
data class AnimeDataDto(
    @param:Json(name = "mal_id") val malId: Int,
    @param:Json(name = "title") val title: String,
    @param:Json(name = "title_english") val titleEnglish: String? = null,
    @param:Json(name = "title_japanese") val titleJapanese: String? = null,
    @param:Json(name = "type") val type: String? = null,
    @param:Json(name = "images") val images: AnimeImagesDto? = null,
    @param:Json(name = "synopsis") val synopsis: String? = null,
    @param:Json(name = "episodes") val episodes: Int? = null,
    @param:Json(name = "score") val score: Double? = null,
    @param:Json(name = "genres") val genres: List<GenreDto>? = null,
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "aired") val aired: AiredDto? = null
)

@JsonClass(generateAdapter = true)
data class AiredDto(
    @param:Json(name = "from") val from: String? = null
)

@JsonClass(generateAdapter = true)
data class AnimeImagesDto(
    @param:Json(name = "jpg") val jpg: ImageUrlDto? = null
)

@JsonClass(generateAdapter = true)
data class ImageUrlDto(
    @param:Json(name = "large_image_url") val largeImageUrl: String? = null,
    @param:Json(name = "image_url") val imageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class GenreDto(
    @param:Json(name = "name") val name: String
)
