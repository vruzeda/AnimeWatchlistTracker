package com.vuzeda.animewatchlist.tracker.domain.repository

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
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
}
