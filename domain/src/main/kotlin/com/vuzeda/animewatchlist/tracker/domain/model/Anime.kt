package com.vuzeda.animewatchlist.tracker.domain.model

data class Anime(
    val id: Long = 0,
    val malId: Int? = null,
    val title: String,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val episodeCount: Int? = null,
    val currentEpisode: Int = 0,
    val score: Double? = null,
    val userRating: Int? = null,
    val status: WatchStatus = WatchStatus.PLAN_TO_WATCH,
    val genres: List<String> = emptyList()
)
