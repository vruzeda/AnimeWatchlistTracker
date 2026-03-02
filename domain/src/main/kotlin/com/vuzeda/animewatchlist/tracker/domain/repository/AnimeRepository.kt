package com.vuzeda.animewatchlist.tracker.domain.repository

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {

    fun observeAll(): Flow<List<Anime>>

    fun observeByStatus(status: WatchStatus): Flow<List<Anime>>

    fun observeById(id: Long): Flow<Anime?>

    fun observeByNotificationEnabled(enabled: Boolean): Flow<List<Anime>>

    suspend fun addAnime(anime: Anime, seasons: List<Season>): Long

    suspend fun updateAnime(anime: Anime)

    suspend fun deleteAnime(id: Long)

    suspend fun toggleNotifications(id: Long, enabled: Boolean)

    suspend fun getNotificationEnabledAnime(): List<Anime>

    suspend fun deleteAllData()
}
