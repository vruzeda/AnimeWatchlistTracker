package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeDataDto

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
