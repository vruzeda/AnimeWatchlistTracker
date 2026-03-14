package com.vuzeda.animewatchlist.tracker.module.domain

data class EpisodePage(
    val episodes: List<EpisodeInfo>,
    val hasNextPage: Boolean,
    val nextPage: Int
)
