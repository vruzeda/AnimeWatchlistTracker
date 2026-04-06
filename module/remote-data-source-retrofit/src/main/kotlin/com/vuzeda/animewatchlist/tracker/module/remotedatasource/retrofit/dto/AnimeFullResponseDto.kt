package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnimeFullResponseDto(
    @Json(name = "data") val data: AnimeFullDataDto
)

@JsonClass(generateAdapter = true)
data class AnimeFullDataDto(
    @Json(name = "mal_id") val malId: Int,
    @Json(name = "title") val title: String,
    @Json(name = "title_english") val titleEnglish: String? = null,
    @Json(name = "title_japanese") val titleJapanese: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "images") val images: AnimeImagesDto? = null,
    @Json(name = "episodes") val episodes: Int? = null,
    @Json(name = "score") val score: Double? = null,
    @Json(name = "synopsis") val synopsis: String? = null,
    @Json(name = "genres") val genres: List<GenreDto>? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "broadcast") val broadcast: BroadcastDto? = null,
    @Json(name = "streaming") val streaming: List<StreamingDto>? = null,
    @Json(name = "relations") val relations: List<AnimeRelationDto>? = null,
    @Json(name = "season") val season: String? = null,
    @Json(name = "year") val year: Int? = null
)

@JsonClass(generateAdapter = true)
data class BroadcastDto(
    @Json(name = "string") val string: String? = null,
    @Json(name = "day") val day: String? = null,
    @Json(name = "time") val time: String? = null,
    @Json(name = "timezone") val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class StreamingDto(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class AnimeRelationDto(
    @Json(name = "relation") val relation: String,
    @Json(name = "entry") val entry: List<RelatedEntryDto>
)

@JsonClass(generateAdapter = true)
data class RelatedEntryDto(
    @Json(name = "mal_id") val malId: Int,
    @Json(name = "type") val type: String,
    @Json(name = "name") val name: String
)
