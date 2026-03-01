package com.vuzeda.animewatchlist.tracker.domain.repository

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeBasicInfo
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.KnownSequel
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {

    fun observeWatchlist(): Flow<List<Anime>>

    fun observeWatchlistByStatus(status: WatchStatus): Flow<List<Anime>>

    suspend fun getAnimeById(id: Long): Anime?

    fun observeAnimeById(id: Long): Flow<Anime?>

    suspend fun addAnime(anime: Anime): Long

    suspend fun updateAnime(anime: Anime)

    suspend fun deleteAnime(id: Long)

    suspend fun searchAnime(query: String): Result<List<Anime>>

    suspend fun getNotifiedAnime(): List<Anime>

    suspend fun fetchAnimeByMalId(malId: Int): Result<Anime>

    suspend fun fetchAnimeFullDetails(malId: Int): Result<AnimeFullDetails>

    suspend fun fetchLastAiredEpisodeNumber(malId: Int): Result<Int?>

    suspend fun fetchAnimeBasicInfo(malId: Int): Result<AnimeBasicInfo>

    suspend fun updateNotificationData(
        id: Long,
        lastCheckedAiredEpisodeCount: Int?,
        knownSequels: List<KnownSequel>
    )

    suspend fun toggleNotifications(id: Long, enabled: Boolean)

    fun observeAnimeByMalIds(malIds: List<Int>): Flow<List<Anime>>
}
