package com.vuzeda.animewatchlist.tracker.module.domain

enum class AnimeSearchType(val apiValue: String?) {
    ALL(null),
    TV("tv"),
    MOVIE("movie"),
    OVA("ova"),
    SPECIAL("special"),
    ONA("ona"),
    MUSIC("music")
}
