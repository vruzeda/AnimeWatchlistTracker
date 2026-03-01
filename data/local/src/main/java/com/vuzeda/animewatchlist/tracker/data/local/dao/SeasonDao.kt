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

    @Query("SELECT * FROM season WHERE animeId = :animeId ORDER BY orderIndex ASC")
    fun observeByAnimeId(animeId: Long): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM season WHERE id = :id")
    fun observeById(id: Long): Flow<SeasonEntity?>

    @Query("SELECT * FROM season WHERE malId = :malId LIMIT 1")
    suspend fun findByMalId(malId: Int): SeasonEntity?

    @Query("SELECT * FROM season WHERE animeId = :animeId ORDER BY orderIndex ASC")
    suspend fun getByAnimeId(animeId: Long): List<SeasonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seasons: List<SeasonEntity>)

    @Update
    suspend fun update(season: SeasonEntity)

    @Query("UPDATE season SET lastCheckedAiredEpisodeCount = :count WHERE id = :seasonId")
    suspend fun updateNotificationData(seasonId: Long, count: Int?)
}
