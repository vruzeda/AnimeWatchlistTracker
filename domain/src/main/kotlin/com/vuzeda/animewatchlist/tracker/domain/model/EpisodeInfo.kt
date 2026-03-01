package com.vuzeda.animewatchlist.tracker.domain.model

data class EpisodeInfo(
    val number: Int,
    val title: String?,
    val aired: String?,
    val isFiller: Boolean,
    val isRecap: Boolean
)
