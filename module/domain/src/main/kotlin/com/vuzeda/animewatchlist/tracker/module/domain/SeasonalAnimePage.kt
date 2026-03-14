package com.vuzeda.animewatchlist.tracker.module.domain

data class SeasonalAnimePage(
    val results: List<SearchResult>,
    val hasNextPage: Boolean,
    val currentPage: Int
)
