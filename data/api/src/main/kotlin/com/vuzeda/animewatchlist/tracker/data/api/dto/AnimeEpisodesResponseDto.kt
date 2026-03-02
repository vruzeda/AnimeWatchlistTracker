package com.vuzeda.animewatchlist.tracker.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnimeEpisodesResponseDto(
    @Json(name = "pagination") val pagination: EpisodesPaginationDto,
    @Json(name = "data") val data: List<EpisodeDto>
)

@JsonClass(generateAdapter = true)
data class EpisodesPaginationDto(
    @Json(name = "last_visible_page") val lastVisiblePage: Int,
    @Json(name = "has_next_page") val hasNextPage: Boolean
)

@JsonClass(generateAdapter = true)
data class EpisodeDto(
    @Json(name = "mal_id") val malId: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "aired") val aired: String? = null,
    @Json(name = "filler") val filler: Boolean = false,
    @Json(name = "recap") val recap: Boolean = false
)
