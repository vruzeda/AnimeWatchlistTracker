package com.vuzeda.animewatchlist.tracker.domain.model

data class EpisodePage(
    val episodes: List<EpisodeInfo>,
    val hasNextPage: Boolean,
    val nextPage: Int
)
