package com.vuzeda.animewatchlist.tracker.module.domain

data class EpisodeInfo(
    val number: Int,
    val titleRomaji: String?,
    val titleEnglish: String?,
    val titleJapanese: String?,
    val aired: String?,
    val isFiller: Boolean,
    val isRecap: Boolean,
    val isPlaceholder: Boolean = false
)