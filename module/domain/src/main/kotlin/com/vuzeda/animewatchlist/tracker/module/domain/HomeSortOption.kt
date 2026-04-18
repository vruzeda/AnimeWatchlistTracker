package com.vuzeda.animewatchlist.tracker.module.domain

enum class HomeSortOption(val defaultAscending: Boolean) {
    ALPHABETICAL(true),
    RECENTLY_ADDED(false),
    USER_RATING(false),
    WATCH_STATUS(true)
}
