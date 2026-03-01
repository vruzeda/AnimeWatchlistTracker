package com.vuzeda.animewatchlist.tracker.domain.model

enum class AnimeSeason(val apiValue: String) {
    WINTER("winter"),
    SPRING("spring"),
    SUMMER("summer"),
    FALL("fall");

    fun next(): Pair<AnimeSeason, Int> = when (this) {
        WINTER -> SPRING to 0
        SPRING -> SUMMER to 0
        SUMMER -> FALL to 0
        FALL -> WINTER to 1
    }

    fun previous(): Pair<AnimeSeason, Int> = when (this) {
        WINTER -> FALL to -1
        SPRING -> WINTER to 0
        SUMMER -> SPRING to 0
        FALL -> SUMMER to 0
    }
}
