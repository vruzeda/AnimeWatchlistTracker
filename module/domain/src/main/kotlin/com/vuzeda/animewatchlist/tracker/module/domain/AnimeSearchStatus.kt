package com.vuzeda.animewatchlist.tracker.module.domain

enum class AnimeSearchStatus(val apiValue: String?) {
    ALL(null),
    AIRING("airing"),
    COMPLETE("complete"),
    UPCOMING("upcoming")
}
