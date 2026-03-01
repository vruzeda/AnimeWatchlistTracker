package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonalAnimePage

fun AnimeSearchResponseDto.toSeasonalAnimePage(currentPage: Int): SeasonalAnimePage =
    SeasonalAnimePage(
        results = data.map { it.toSearchResult() },
        hasNextPage = pagination?.hasNextPage ?: false,
        currentPage = currentPage
    )
