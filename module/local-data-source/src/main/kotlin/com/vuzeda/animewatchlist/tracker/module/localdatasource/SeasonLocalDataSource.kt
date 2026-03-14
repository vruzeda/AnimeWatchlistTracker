package com.vuzeda.animewatchlist.tracker.module.localdatasource

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import kotlinx.coroutines.flow.Flow

interface SeasonLocalDataSource {
    fun observeAll(): Flow<List<Season>>
    fun observeByAnimeId(animeId: Long): Flow<List<Season>>
    fun observeById(id: Long): Flow<Season?>
    suspend fun findByMalId(malId: Int): Season?
    suspend fun getByAnimeId(animeId: Long): List<Season>
    suspend fun insertAll(seasons: List<Season>)
    suspend fun deleteById(id: Long)
    suspend fun update(season: Season)
    suspend fun updateNotificationData(seasonId: Long, count: Int?)
    suspend fun updateEpisodeNotificationsEnabled(seasonId: Long, enabled: Boolean)
    suspend fun getSeasonsWithEpisodeNotifications(): List<Season>
    fun observeAllMalIds(): Flow<List<Int>>
}
