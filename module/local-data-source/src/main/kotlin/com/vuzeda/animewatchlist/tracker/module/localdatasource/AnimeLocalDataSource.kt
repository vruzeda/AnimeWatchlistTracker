package com.vuzeda.animewatchlist.tracker.module.localdatasource

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface AnimeLocalDataSource {
    fun observeAll(): Flow<List<Anime>>
    fun observeById(id: Long): Flow<Anime?>
    suspend fun insert(anime: Anime): Long
    suspend fun update(anime: Anime)
    suspend fun deleteById(id: Long)
    suspend fun deleteAll()
    suspend fun getById(id: Long): Anime?
    suspend fun getNotificationEnabledAnime(): List<Anime>
    suspend fun updateNotificationType(id: Long, notificationType: NotificationType)
    suspend fun updateLastSeasonCheckDateForAll(date: LocalDate)
}
