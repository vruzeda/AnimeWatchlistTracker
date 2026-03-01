package com.vuzeda.animewatchlist.tracker.domain.model

data class ResolvedSeries(
    val title: String,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val seasons: List<SeasonData>
)
