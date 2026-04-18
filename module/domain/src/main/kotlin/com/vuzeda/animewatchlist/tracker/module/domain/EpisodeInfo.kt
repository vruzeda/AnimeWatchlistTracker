package com.vuzeda.animewatchlist.tracker.module.domain

data class EpisodeInfo(
    val number: Int,
    val title: String?,
    val aired: String?,
    val isFiller: Boolean,
    val isRecap: Boolean,
    val isPlaceholder: Boolean = false
)
