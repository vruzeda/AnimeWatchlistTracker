package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnimeFullResponseDto(
    @param:Json(name = "data") val data: AnimeFullDataDto
)

@JsonClass(generateAdapter = true)
data class AnimeFullDataDto(
    @param:Json(name = "mal_id") val malId: Int,
    @param:Json(name = "title") val title: String,
    @param:Json(name = "title_english") val titleEnglish: String? = null,
    @param:Json(name = "title_japanese") val titleJapanese: String? = null,
    @param:Json(name = "type") val type: String? = null,
    @param:Json(name = "images") val images: AnimeImagesDto? = null,
    @param:Json(name = "episodes") val episodes: Int? = null,
    @param:Json(name = "score") val score: Double? = null,
    @param:Json(name = "synopsis") val synopsis: String? = null,
    @param:Json(name = "genres") val genres: List<GenreDto>? = null,
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "broadcast") val broadcast: BroadcastDto? = null,
    @param:Json(name = "relations") val relations: List<AnimeRelationDto>? = null
)

@JsonClass(generateAdapter = true)
data class BroadcastDto(
    @param:Json(name = "string") val string: String? = null
)

@JsonClass(generateAdapter = true)
data class AnimeRelationDto(
    @param:Json(name = "relation") val relation: String,
    @param:Json(name = "entry") val entry: List<RelatedEntryDto>
)

@JsonClass(generateAdapter = true)
data class RelatedEntryDto(
    @param:Json(name = "mal_id") val malId: Int,
    @param:Json(name = "type") val type: String,
    @param:Json(name = "name") val name: String
)
