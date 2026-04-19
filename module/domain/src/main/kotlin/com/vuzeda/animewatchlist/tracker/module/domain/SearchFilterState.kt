package com.vuzeda.animewatchlist.tracker.module.domain

data class SearchFilterState(
    val type: AnimeSearchType = AnimeSearchType.ALL,
    val status: AnimeSearchStatus = AnimeSearchStatus.ALL,
    val orderBy: AnimeSearchOrderBy = AnimeSearchOrderBy.DEFAULT,
    val isAscending: Boolean = AnimeSearchOrderBy.DEFAULT.defaultAscending
)
