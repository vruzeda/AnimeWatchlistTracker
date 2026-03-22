package com.vuzeda.animewatchlist.tracker.module.localdatasource

import kotlinx.coroutines.flow.Flow

interface SchedulerLocalDataSource {
    fun observeLastAnimeUpdateRun(): Flow<Long?>
    suspend fun setLastAnimeUpdateRun(epochMillis: Long)
}
