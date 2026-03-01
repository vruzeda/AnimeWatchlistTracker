package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeBasicInfo

fun AnimeDataDto.toAnimeBasicInfo(): AnimeBasicInfo = AnimeBasicInfo(
    malId = malId,
    status = status ?: "Unknown",
    airedFrom = aired?.from
)
