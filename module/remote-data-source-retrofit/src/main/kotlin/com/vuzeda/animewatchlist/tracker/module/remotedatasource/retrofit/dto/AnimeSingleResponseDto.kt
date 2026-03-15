package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnimeSingleResponseDto(
    @param:Json(name = "data") val data: AnimeDataDto
)
