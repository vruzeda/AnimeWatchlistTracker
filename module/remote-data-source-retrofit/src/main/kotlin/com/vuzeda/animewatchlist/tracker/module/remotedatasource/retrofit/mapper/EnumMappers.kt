package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchOrderBy
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType

internal fun AnimeSearchType.toApiValue(): String? = when (this) {
    AnimeSearchType.ALL -> null
    AnimeSearchType.TV -> "tv"
    AnimeSearchType.MOVIE -> "movie"
    AnimeSearchType.OVA -> "ova"
    AnimeSearchType.SPECIAL -> "special"
    AnimeSearchType.ONA -> "ona"
    AnimeSearchType.MUSIC -> "music"
}

internal fun AnimeSearchStatus.toApiValue(): String? = when (this) {
    AnimeSearchStatus.ALL -> null
    AnimeSearchStatus.AIRING -> "airing"
    AnimeSearchStatus.COMPLETE -> "complete"
    AnimeSearchStatus.UPCOMING -> "upcoming"
}

internal fun AnimeSearchOrderBy.toApiValue(): String? = when (this) {
    AnimeSearchOrderBy.DEFAULT -> null
    AnimeSearchOrderBy.SCORE -> "score"
    AnimeSearchOrderBy.RANK -> "rank"
    AnimeSearchOrderBy.POPULARITY -> "popularity"
    AnimeSearchOrderBy.MEMBERS -> "members"
    AnimeSearchOrderBy.FAVORITES -> "favorites"
    AnimeSearchOrderBy.START_DATE -> "start_date"
    AnimeSearchOrderBy.TITLE -> "title"
}

internal fun AnimeSeason.toApiValue(): String = when (this) {
    AnimeSeason.WINTER -> "winter"
    AnimeSeason.SPRING -> "spring"
    AnimeSeason.SUMMER -> "summer"
    AnimeSeason.FALL -> "fall"
}

internal fun String.toAnimeSeason(): AnimeSeason? =
    AnimeSeason.entries.firstOrNull { it.toApiValue().equals(this, ignoreCase = true) }
