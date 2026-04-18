package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ChiakiWatchOrderEntryDto
import java.time.LocalDate

private val TYPE_CODE_TO_STRING = mapOf(
    1 to "TV",
    2 to "OVA",
    3 to "Movie",
    4 to "Special",
    5 to "ONA",
    6 to "Music",
    7 to "CM",
    8 to "PV",
    9 to "TV Special",
)

fun List<ChiakiWatchOrderEntryDto>.toSeasonDataList(): List<SeasonData> =
    map { it.toSeasonData() }

fun ChiakiWatchOrderEntryDto.toSeasonData(): SeasonData = SeasonData(
    malId = malId,
    title = title,
    titleEnglish = titleEnglish,
    imageUrl = imageUrl,
    type = TYPE_CODE_TO_STRING[typeCode] ?: "TV",
    episodeCount = episodeCount,
    score = score,
    isMainSeries = isMainSeries,
    startDate = startDate,
    airingStatus = inferAiringStatus(startDate, endDate),
)

private fun inferAiringStatus(startDate: LocalDate?, endDate: LocalDate?): String? {
    val today = LocalDate.now()
    return when {
        startDate == null                          -> null
        startDate.isAfter(today)                   -> "Not yet aired"
        endDate != null && !endDate.isAfter(today) -> "Finished Airing"
        else                                       -> "Currently Airing"
    }
}
