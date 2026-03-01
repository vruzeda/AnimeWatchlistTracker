package com.vuzeda.animewatchlist.tracker.domain.model

data class AnimeFullDetails(
    val malId: Int,
    val episodes: Int?,
    val sequels: List<SequelInfo>,
    val prequels: List<SequelInfo> = emptyList()
)

data class SequelInfo(
    val malId: Int,
    val title: String
)
