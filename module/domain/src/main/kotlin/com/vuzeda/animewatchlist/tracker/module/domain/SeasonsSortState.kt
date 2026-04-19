package com.vuzeda.animewatchlist.tracker.module.domain

data class SeasonsSortState(
    val option: SeasonsSortOption = SeasonsSortOption.DEFAULT,
    val isAscending: Boolean = SeasonsSortOption.DEFAULT.defaultAscending
)
