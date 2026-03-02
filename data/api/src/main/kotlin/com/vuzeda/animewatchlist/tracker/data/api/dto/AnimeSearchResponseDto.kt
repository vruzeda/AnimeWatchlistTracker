package com.vuzeda.animewatchlist.tracker.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchPaginationDto(
    @Json(name = "has_next_page") val hasNextPage: Boolean = false,
    @Json(name = "last_visible_page") val lastVisiblePage: Int = 1
)

@JsonClass(generateAdapter = true)
data class AnimeSearchResponseDto(
    @Json(name = "pagination") val pagination: SearchPaginationDto? = null,
    @Json(name = "data") val data: List<AnimeDataDto>
)

@JsonClass(generateAdapter = true)
data class AnimeDataDto(
    @Json(name = "mal_id") val malId: Int,
    @Json(name = "title") val title: String,
    @Json(name = "title_english") val titleEnglish: String? = null,
    @Json(name = "title_japanese") val titleJapanese: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "images") val images: AnimeImagesDto? = null,
    @Json(name = "synopsis") val synopsis: String? = null,
    @Json(name = "episodes") val episodes: Int? = null,
    @Json(name = "score") val score: Double? = null,
    @Json(name = "genres") val genres: List<GenreDto>? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "aired") val aired: AiredDto? = null
)

@JsonClass(generateAdapter = true)
data class AiredDto(
    @Json(name = "from") val from: String? = null
)

@JsonClass(generateAdapter = true)
data class AnimeImagesDto(
    @Json(name = "jpg") val jpg: ImageUrlDto? = null
)

@JsonClass(generateAdapter = true)
data class ImageUrlDto(
    @Json(name = "large_image_url") val largeImageUrl: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class GenreDto(
    @Json(name = "name") val name: String
)
