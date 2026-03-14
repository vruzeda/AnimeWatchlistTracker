package com.vuzeda.animewatchlist.tracker.data.local

data class Anime(
    val id: Long = 0,
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val genres: String = "",
    val status: String,
    val userRating: Int? = null,
    val notificationType: String = "NONE",
    val addedAt: Long = 0
)
