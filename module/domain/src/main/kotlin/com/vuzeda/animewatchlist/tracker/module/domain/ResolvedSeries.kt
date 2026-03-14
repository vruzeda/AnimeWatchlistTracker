package com.vuzeda.animewatchlist.tracker.module.domain

data class ResolvedSeries(
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val seasons: List<SeasonData>
)
