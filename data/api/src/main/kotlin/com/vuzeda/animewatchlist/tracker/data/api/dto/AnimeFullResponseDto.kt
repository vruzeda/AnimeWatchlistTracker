package com.vuzeda.animewatchlist.tracker.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AnimeFullResponseDto(
    @Json(name = "data") val data: AnimeFullDataDto
)

@JsonClass(generateAdapter = false)
data class AnimeFullDataDto(
    @Json(name = "mal_id") val malId: Int,
    @Json(name = "title") val title: String,
    @Json(name = "episodes") val episodes: Int? = null,
    @Json(name = "relations") val relations: List<AnimeRelationDto>? = null
)

@JsonClass(generateAdapter = false)
data class AnimeRelationDto(
    @Json(name = "relation") val relation: String,
    @Json(name = "entry") val entry: List<RelatedEntryDto>
)

@JsonClass(generateAdapter = false)
data class RelatedEntryDto(
    @Json(name = "mal_id") val malId: Int,
    @Json(name = "type") val type: String,
    @Json(name = "name") val name: String
)
