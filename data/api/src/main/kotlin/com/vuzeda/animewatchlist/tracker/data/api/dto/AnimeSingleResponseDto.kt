package com.vuzeda.animewatchlist.tracker.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AnimeSingleResponseDto(
    @param:Json(name = "data") val data: AnimeDataDto
)
