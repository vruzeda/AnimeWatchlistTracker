package com.vuzeda.animewatchlist.tracker.module.domain

enum class AnimeSearchOrderBy(val apiValue: String?, val defaultAscending: Boolean) {
    DEFAULT(null, true),
    SCORE("score", false),
    RANK("rank", true),
    POPULARITY("popularity", true),
    MEMBERS("members", false),
    FAVORITES("favorites", false),
    START_DATE("start_date", false),
    TITLE("title", true)
}
