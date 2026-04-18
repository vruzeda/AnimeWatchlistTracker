package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto

import java.time.LocalDate

data class ChiakiWatchOrderEntryDto(
    val malId: Int,
    val title: String,
    val titleEnglish: String? = null,
    val typeCode: Int,
    val episodeCount: Int?,
    val score: Double?,
    val imageUrl: String?,
    var isMainSeries: Boolean = true,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
