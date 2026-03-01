package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult

fun AnimeDataDto.toSearchResult(): SearchResult = SearchResult(
    malId = malId,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl,
    synopsis = synopsis,
    episodeCount = episodes,
    score = score,
    type = type,
    genres = genres?.map { it.name } ?: emptyList()
)
