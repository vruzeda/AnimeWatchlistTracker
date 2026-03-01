package com.vuzeda.animewatchlist.tracker.data.api.dto

data class ChiakiWatchOrderEntryDto(
    val malId: Int,
    val title: String,
    val typeCode: Int,
    val episodeCount: Int?,
    val score: Double?,
    val imageUrl: String?
)
