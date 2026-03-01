package com.vuzeda.animewatchlist.tracker.domain.model

data class SearchResult(
    val malId: Int,
    val title: String,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val episodeCount: Int? = null,
    val score: Double? = null,
    val type: String? = null,
    val genres: List<String> = emptyList()
)
