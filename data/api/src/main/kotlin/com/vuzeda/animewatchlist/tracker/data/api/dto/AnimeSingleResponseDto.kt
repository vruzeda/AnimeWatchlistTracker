package com.vuzeda.animewatchlist.tracker.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AnimeSingleResponseDto(
    @Json(name = "data") val data: AnimeDataDto
)
