package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.api.dto.ChiakiWatchOrderEntryDto
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonData

private val ALLOWED_TYPE_CODES = setOf(1, 3)

private val TYPE_CODE_TO_STRING = mapOf(
    1 to "TV",
    3 to "Movie"
)

fun List<ChiakiWatchOrderEntryDto>.toSeasonDataList(): List<SeasonData> =
    filter { it.typeCode in ALLOWED_TYPE_CODES }
        .map { it.toSeasonData() }

fun ChiakiWatchOrderEntryDto.toSeasonData(): SeasonData = SeasonData(
    malId = malId,
    title = title,
    titleEnglish = titleEnglish,
    imageUrl = imageUrl,
    type = TYPE_CODE_TO_STRING[typeCode] ?: "TV",
    episodeCount = episodeCount,
    score = score
)
