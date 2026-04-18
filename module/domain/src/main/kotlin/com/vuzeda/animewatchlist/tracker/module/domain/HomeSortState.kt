package com.vuzeda.animewatchlist.tracker.module.domain

data class HomeSortState(
    val option: HomeSortOption = HomeSortOption.ALPHABETICAL,
    val isAscending: Boolean = HomeSortOption.ALPHABETICAL.defaultAscending
)
