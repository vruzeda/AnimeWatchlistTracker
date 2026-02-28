package com.vuzeda.animewatchlist.tracker.domain.repository

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {

    fun observeWatchlist(): Flow<List<Anime>>

    fun observeWatchlistByStatus(status: WatchStatus): Flow<List<Anime>>

    suspend fun getAnimeById(id: Long): Anime?

    suspend fun addAnime(anime: Anime): Long

    suspend fun updateAnime(anime: Anime)

    suspend fun deleteAnime(id: Long)

    suspend fun searchAnime(query: String): Result<List<Anime>>

    suspend fun getNotifiedAnime(): List<Anime>

    suspend fun fetchAnimeFullDetails(malId: Int): Result<AnimeFullDetails>

    suspend fun updateNotificationData(
        id: Long,
        lastCheckedEpisodeCount: Int?,
        knownSequelMalIds: List<Int>
    )

    suspend fun toggleNotifications(id: Long, enabled: Boolean)

    suspend fun getAnimeByMalIds(malIds: List<Int>): List<Anime>
}
