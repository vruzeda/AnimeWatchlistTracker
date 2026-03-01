package com.vuzeda.animewatchlist.tracker.domain.model

data class Anime(
    val id: Long = 0,
    val title: String,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val status: WatchStatus = WatchStatus.PLAN_TO_WATCH,
    val userRating: Int? = null,
    val isNotificationsEnabled: Boolean = false,
    val addedAt: Long = 0
)
