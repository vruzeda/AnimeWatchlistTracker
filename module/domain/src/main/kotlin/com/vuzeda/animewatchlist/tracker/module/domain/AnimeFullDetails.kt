package com.vuzeda.animewatchlist.tracker.module.domain

data class AnimeFullDetails(
    val malId: Int,
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val type: String,
    val episodes: Int?,
    val score: Double? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val airingStatus: String? = null,
    val broadcastInfo: String? = null,
    val sequels: List<SequelInfo>,
    val prequels: List<SequelInfo> = emptyList()
)

data class SequelInfo(
    val malId: Int,
    val title: String
)
