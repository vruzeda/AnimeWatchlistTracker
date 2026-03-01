package com.vuzeda.animewatchlist.tracker.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AnimeFullResponseDto(
    @param:Json(name = "data") val data: AnimeFullDataDto
)

@JsonClass(generateAdapter = false)
data class AnimeFullDataDto(
    @param:Json(name = "mal_id") val malId: Int,
    @param:Json(name = "title") val title: String,
    @param:Json(name = "episodes") val episodes: Int? = null,
    @param:Json(name = "relations") val relations: List<AnimeRelationDto>? = null
)

@JsonClass(generateAdapter = false)
data class AnimeRelationDto(
    @param:Json(name = "relation") val relation: String,
    @param:Json(name = "entry") val entry: List<RelatedEntryDto>
)

@JsonClass(generateAdapter = false)
data class RelatedEntryDto(
    @param:Json(name = "mal_id") val malId: Int,
    @param:Json(name = "type") val type: String,
    @param:Json(name = "name") val name: String
)
