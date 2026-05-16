package com.vuzeda.animewatchlist.tracker.module.domain

data class Feedback(
    val category: FeedbackCategory,
    val message: String,
    val appVersion: String,
    val timestamp: Long,
    val deviceModel: String,
    val androidVersion: Int,
    val installationId: String,
    val titleLanguage: String,
    val homeViewMode: String,
    val contactName: String? = null,
    val contactEmail: String? = null
)

enum class FeedbackCategory {
    BUG_REPORT,
    FEATURE_REQUEST,
    GENERAL
}
