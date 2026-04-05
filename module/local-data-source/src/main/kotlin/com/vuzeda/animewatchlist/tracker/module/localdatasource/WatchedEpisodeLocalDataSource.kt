package com.vuzeda.animewatchlist.tracker.module.localdatasource

import kotlinx.coroutines.flow.Flow

interface WatchedEpisodeLocalDataSource {
    fun observeWatchedEpisodeNumbers(seasonId: Long): Flow<Set<Int>>
    suspend fun markWatched(seasonId: Long, episodeNumber: Int)
    suspend fun markUnwatched(seasonId: Long, episodeNumber: Int)
}
