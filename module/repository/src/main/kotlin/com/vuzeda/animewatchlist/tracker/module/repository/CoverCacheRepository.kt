package com.vuzeda.animewatchlist.tracker.module.repository

interface CoverCacheRepository {
    suspend fun getCoverCacheSize(): Long
    suspend fun clearCoverCache()
}
