package com.vuzeda.animewatchlist.tracker.module.domain

sealed interface AnimeUpdateResult {
    data object Success : AnimeUpdateResult
    data class Failure(val reason: String?) : AnimeUpdateResult
    data class WillRetry(val reason: String?, val retryCount: Int?) : AnimeUpdateResult
}
