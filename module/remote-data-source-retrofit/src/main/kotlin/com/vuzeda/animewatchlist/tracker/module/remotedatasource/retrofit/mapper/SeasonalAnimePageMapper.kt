package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeSearchResponseDto

fun AnimeSearchResponseDto.toSeasonalAnimePage(currentPage: Int): SeasonalAnimePage =
    SeasonalAnimePage(
        results = data.map { it.toSearchResult() }.distinctBy { it.malId },
        hasNextPage = pagination?.hasNextPage ?: false,
        currentPage = currentPage
    )
