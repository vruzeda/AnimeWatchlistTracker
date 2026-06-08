package com.vuzeda.animewatchlist.tracker.module.scheduler

interface AnimeUpdateScheduler {
    fun schedulePeriodicUpdate()
    fun scheduleImmediateUpdate()
    fun scheduleAiringSeasonBackfill()
    fun scheduleRetryAfterRateLimit(delayMs: Long)
}
