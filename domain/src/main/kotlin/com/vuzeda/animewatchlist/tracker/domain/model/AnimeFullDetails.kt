package com.vuzeda.animewatchlist.tracker.domain.model

data class AnimeFullDetails(
    val malId: Int,
    val title: String,
    val imageUrl: String? = null,
    val type: String,
    val episodes: Int?,
    val score: Double? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val airingStatus: String? = null,
    val sequels: List<SequelInfo>,
    val prequels: List<SequelInfo> = emptyList()
)

data class SequelInfo(
    val malId: Int,
    val title: String
)
