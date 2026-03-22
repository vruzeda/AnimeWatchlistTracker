package com.vuzeda.animewatchlist.tracker.module.repository

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface SchedulerRepository {
    fun schedulePeriodicAnimeUpdate()
    fun scheduleImmediateAnimeUpdate()
    fun observeLastAnimeUpdateRun(): Flow<Instant?>
    suspend fun recordAnimeUpdateRun()
}
