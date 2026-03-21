package com.vuzeda.animewatchlist.tracker.module.domain

import java.time.LocalDate

data class Season(
    val id: Long = 0,
    val animeId: Long = 0,
    val malId: Int,
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val type: String = "TV",
    val episodeCount: Int? = null,
    val currentEpisode: Int = 0,
    val status: WatchStatus = WatchStatus.PLAN_TO_WATCH,
    val score: Double? = null,
    val orderIndex: Int = 0,
    val airingStatus: String? = null,
    val broadcastInfo: String? = null,
    val lastCheckedAiredEpisodeCount: Int? = null,
    val lastEpisodeCheckDate: LocalDate? = null,
    val isEpisodeNotificationsEnabled: Boolean = false,
    val isInWatchlist: Boolean = true
)
