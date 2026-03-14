package com.vuzeda.animewatchlist.tracker.module.domain

data class Anime(
    val id: Long = 0,
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val status: WatchStatus = WatchStatus.PLAN_TO_WATCH,
    val userRating: Int? = null,
    val notificationType: NotificationType = NotificationType.NONE,
    val addedAt: Long = 0
) {
    val isNotificationsEnabled: Boolean get() = notificationType != NotificationType.NONE
}
