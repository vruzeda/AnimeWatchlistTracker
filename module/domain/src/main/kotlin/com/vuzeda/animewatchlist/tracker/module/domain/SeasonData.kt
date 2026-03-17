package com.vuzeda.animewatchlist.tracker.module.domain

import java.time.LocalDate

data class SeasonData(
    val malId: Int,
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val type: String,
    val episodeCount: Int? = null,
    val score: Double? = null,
    val airingStatus: String? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val isMainSeries: Boolean = true,
    val startDate: LocalDate? = null,
)