package com.vuzeda.animewatchlist.tracker.domain.model

data class ProgressiveResolveResult(
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val seasons: List<SeasonData>,
    val isResolvingPrequels: Boolean,
    val isResolvingSequels: Boolean
)
