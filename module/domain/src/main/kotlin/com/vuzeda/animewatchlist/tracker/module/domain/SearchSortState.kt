package com.vuzeda.animewatchlist.tracker.module.domain

data class SearchSortState(
    val option: SearchSortOption = SearchSortOption.DEFAULT,
    val isAscending: Boolean = SearchSortOption.DEFAULT.defaultAscending
)
