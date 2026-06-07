package com.vuzeda.animewatchlist.tracker.module.domain

import kotlin.time.Instant

data class AnimeUpdateSchedulerState(
    val lastSuccessfulRunAt: Instant?,
    val lastAttemptAt: Instant?,
    val lastAttemptResult: AnimeUpdateResult?
)
