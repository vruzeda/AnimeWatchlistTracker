package com.vuzeda.animewatchlist.tracker.domain.model

data class SeasonalAnimePage(
    val results: List<SearchResult>,
    val hasNextPage: Boolean,
    val currentPage: Int
)
