package com.vuzeda.animewatchlist.tracker.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AnimeEpisodesResponseDto(
    @param:Json(name = "pagination") val pagination: EpisodesPaginationDto,
    @param:Json(name = "data") val data: List<EpisodeDto>
)

@JsonClass(generateAdapter = false)
data class EpisodesPaginationDto(
    @param:Json(name = "last_visible_page") val lastVisiblePage: Int,
    @param:Json(name = "has_next_page") val hasNextPage: Boolean
)

@JsonClass(generateAdapter = false)
data class EpisodeDto(
    @param:Json(name = "mal_id") val malId: Int,
    @param:Json(name = "aired") val aired: String? = null
)
