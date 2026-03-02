package com.vuzeda.animewatchlist.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vuzeda.animewatchlist.tracker.data.local.entity.SeasonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonDao {

    @Query("SELECT * FROM season ORDER BY animeId, orderIndex ASC")
    fun observeAll(): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM season WHERE animeId = :animeId ORDER BY orderIndex ASC")
    fun observeByAnimeId(animeId: Long): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM season WHERE id = :id")
    fun observeById(id: Long): Flow<SeasonEntity?>

    @Query("SELECT * FROM season WHERE malId = :malId LIMIT 1")
    suspend fun findByMalId(malId: Int): SeasonEntity?

    @Query("SELECT malId, animeId FROM season WHERE malId IN (:malIds)")
    suspend fun findAnimeIdsByMalIds(malIds: List<Int>): List<SeasonMalIdProjection>

    @Query("SELECT * FROM season WHERE animeId = :animeId ORDER BY orderIndex ASC")
    suspend fun getByAnimeId(animeId: Long): List<SeasonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seasons: List<SeasonEntity>)

    @Update
    suspend fun update(season: SeasonEntity)

    @Query("UPDATE season SET lastCheckedAiredEpisodeCount = :count WHERE id = :seasonId")
    suspend fun updateNotificationData(seasonId: Long, count: Int?)

    @Query("UPDATE season SET isEpisodeNotificationsEnabled = :enabled WHERE id = :seasonId")
    suspend fun updateEpisodeNotificationsEnabled(seasonId: Long, enabled: Boolean)

    @Query("SELECT * FROM season WHERE isEpisodeNotificationsEnabled = 1")
    suspend fun getSeasonsWithEpisodeNotifications(): List<SeasonEntity>

    @Query("SELECT malId FROM season")
    fun observeAllMalIds(): Flow<List<Int>>
}

data class SeasonMalIdProjection(
    val malId: Int,
    val animeId: Long
)
