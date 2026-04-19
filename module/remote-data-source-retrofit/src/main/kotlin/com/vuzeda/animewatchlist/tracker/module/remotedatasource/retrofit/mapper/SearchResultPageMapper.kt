package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeSearchResponseDto

fun AnimeSearchResponseDto.toSearchResultPage(currentPage: Int): SearchResultPage =
    SearchResultPage(
        results = data.map { it.toSearchResult() }.distinctBy { it.malId },
        hasNextPage = pagination?.hasNextPage ?: false,
        currentPage = currentPage
    )
