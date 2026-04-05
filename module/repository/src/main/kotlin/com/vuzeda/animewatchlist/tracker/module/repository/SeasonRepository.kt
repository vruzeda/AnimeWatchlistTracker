package com.vuzeda.animewatchlist.tracker.module.repository

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


interface SeasonRepository {

    fun observeAllSeasons(): Flow<List<Season>>

    fun observeAllSeasonMalIds(): Flow<Set<Int>>

    fun observeSeasonsForAnime(animeId: Long): Flow<List<Season>>

    fun observeSeasonById(id: Long): Flow<Season?>

    suspend fun findAnimeIdBySeasonMalId(malId: Int): Long?

    suspend fun findSeasonIdByMalId(malId: Int): Long?

    suspend fun getSeasonsForAnime(animeId: Long): List<Season>

    suspend fun deleteSeason(id: Long)

    suspend fun addSeasonsToAnime(animeId: Long, seasons: List<Season>)

    suspend fun updateSeason(season: Season)

    suspend fun updateSeasonNotificationData(
        seasonId: Long,
        lastCheckedAiredEpisodeCount: Int?
    )

    suspend fun toggleSeasonEpisodeNotifications(seasonId: Long, enabled: Boolean)

    suspend fun getSeasonsWithEpisodeNotifications(): List<Season>

    suspend fun updateLastEpisodeCheckDate(seasonId: Long, date: LocalDate)

    fun observeWatchedEpisodesForSeason(seasonId: Long): Flow<Set<Int>>

    suspend fun setEpisodeWatched(seasonId: Long, episodeNumber: Int, isWatched: Boolean)
}
