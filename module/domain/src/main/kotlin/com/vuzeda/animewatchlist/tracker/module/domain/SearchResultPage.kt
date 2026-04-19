package com.vuzeda.animewatchlist.tracker.module.domain

data class SearchResultPage(
    val results: List<SearchResult>,
    val hasNextPage: Boolean,
    val currentPage: Int
)
