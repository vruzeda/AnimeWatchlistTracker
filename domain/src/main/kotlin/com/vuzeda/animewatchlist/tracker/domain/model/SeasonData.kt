package com.vuzeda.animewatchlist.tracker.domain.model

data class SeasonData(
    val malId: Int,
    val title: String,
    val imageUrl: String? = null,
    val type: String,
    val episodeCount: Int? = null,
    val score: Double? = null,
    val airingStatus: String? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList()
)
