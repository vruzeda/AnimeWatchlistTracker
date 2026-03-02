package com.vuzeda.animewatchlist.tracker.domain.repository

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import kotlinx.coroutines.flow.Flow

interface SeasonRepository {

    fun observeSeasonsForAnime(animeId: Long): Flow<List<Season>>

    fun observeSeasonById(id: Long): Flow<Season?>

    suspend fun findAnimeIdBySeasonMalId(malId: Int): Long?

    suspend fun findAnimeIdsBySeasonMalIds(malIds: List<Int>): Map<Int, Long>

    suspend fun getSeasonsForAnime(animeId: Long): List<Season>

    suspend fun addSeasonsToAnime(animeId: Long, seasons: List<Season>)

    suspend fun updateSeason(season: Season)

    suspend fun updateSeasonNotificationData(
        seasonId: Long,
        lastCheckedAiredEpisodeCount: Int?
    )
}
