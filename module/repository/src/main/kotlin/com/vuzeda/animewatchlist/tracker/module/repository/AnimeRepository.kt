package com.vuzeda.animewatchlist.tracker.module.repository

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {

    fun observeAll(): Flow<List<Anime>>

    fun observeByStatus(status: WatchStatus): Flow<List<Anime>>

    fun observeById(id: Long): Flow<Anime?>

    suspend fun getAnimeById(id: Long): Anime?

    suspend fun addAnime(anime: Anime, seasons: List<Season>): Long

    suspend fun updateAnime(anime: Anime)

    suspend fun deleteAnime(id: Long)

    suspend fun updateNotificationType(id: Long, notificationType: NotificationType)

    suspend fun getNotificationEnabledAnime(): List<Anime>

    suspend fun deleteAllData()

    suspend fun searchAnime(query: String): Result<List<SearchResult>>

    suspend fun fetchAnimeFullById(malId: Int): Result<AnimeFullDetails>

    suspend fun fetchAnimeEpisodes(malId: Int, page: Int): Result<EpisodePage>

    suspend fun fetchLastAiredEpisodeNumber(malId: Int, today: LocalDate): Result<Int?>

    suspend fun fetchWatchOrder(malId: Int): Result<List<SeasonData>>

    suspend fun fetchSeasonAnime(year: Int, season: AnimeSeason, page: Int): Result<SeasonalAnimePage>
}
