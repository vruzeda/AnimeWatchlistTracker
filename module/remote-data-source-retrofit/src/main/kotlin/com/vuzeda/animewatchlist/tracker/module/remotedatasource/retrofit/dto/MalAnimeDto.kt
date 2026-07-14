package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MalAnimeListResponseDto(
    @Json(name = "data") val data: List<MalAnimeNodeWrapperDto> = emptyList(),
    @Json(name = "paging") val paging: MalPagingDto? = null
)

@JsonClass(generateAdapter = true)
data class MalAnimeNodeWrapperDto(
    @Json(name = "node") val node: MalAnimeDto
)

@JsonClass(generateAdapter = true)
data class MalPagingDto(
    @Json(name = "next") val next: String? = null
)

@JsonClass(generateAdapter = true)
data class MalAnimeDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "alternative_titles") val alternativeTitles: MalAlternativeTitlesDto? = null,
    @Json(name = "main_picture") val mainPicture: MalMainPictureDto? = null,
    @Json(name = "media_type") val mediaType: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "num_episodes") val numEpisodes: Int? = null,
    @Json(name = "mean") val mean: Double? = null,
    @Json(name = "synopsis") val synopsis: String? = null,
    @Json(name = "genres") val genres: List<MalGenreDto>? = null,
    @Json(name = "broadcast") val broadcast: MalBroadcastDto? = null,
    @Json(name = "start_season") val startSeason: MalStartSeasonDto? = null,
    @Json(name = "related_anime") val relatedAnime: List<MalRelatedAnimeDto>? = null
)

@JsonClass(generateAdapter = true)
data class MalAlternativeTitlesDto(
    @Json(name = "en") val en: String? = null,
    @Json(name = "ja") val ja: String? = null
)

@JsonClass(generateAdapter = true)
data class MalMainPictureDto(
    @Json(name = "medium") val medium: String? = null,
    @Json(name = "large") val large: String? = null
)

@JsonClass(generateAdapter = true)
data class MalGenreDto(
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class MalBroadcastDto(
    @Json(name = "day_of_the_week") val dayOfTheWeek: String? = null,
    @Json(name = "start_time") val startTime: String? = null
)

@JsonClass(generateAdapter = true)
data class MalStartSeasonDto(
    @Json(name = "year") val year: Int? = null,
    @Json(name = "season") val season: String? = null
)

@JsonClass(generateAdapter = true)
data class MalRelatedAnimeDto(
    @Json(name = "node") val node: MalRelatedNodeDto,
    @Json(name = "relation_type") val relationType: String? = null
)

@JsonClass(generateAdapter = true)
data class MalRelatedNodeDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String
)
