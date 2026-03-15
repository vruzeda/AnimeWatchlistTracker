package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnimeEpisodesResponseDto(
    @param:Json(name = "pagination") val pagination: EpisodesPaginationDto,
    @param:Json(name = "data") val data: List<EpisodeDto>
)

@JsonClass(generateAdapter = true)
data class EpisodesPaginationDto(
    @param:Json(name = "last_visible_page") val lastVisiblePage: Int,
    @param:Json(name = "has_next_page") val hasNextPage: Boolean
)

@JsonClass(generateAdapter = true)
data class EpisodeDto(
    @param:Json(name = "mal_id") val malId: Int,
    @param:Json(name = "title") val title: String? = null,
    @param:Json(name = "aired") val aired: String? = null,
    @param:Json(name = "filler") val filler: Boolean = false,
    @param:Json(name = "recap") val recap: Boolean = false
)
