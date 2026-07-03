package com.vuzeda.animewatchlist.tracker.module.domain

enum class AnimeSearchOrderBy(val defaultAscending: Boolean) {
    DEFAULT(true),
    SCORE(false),
    RANK(true),
    POPULARITY(true),
    MEMBERS(false),
    FAVORITES(false),
    START_DATE(false),
    TITLE(true)
}
