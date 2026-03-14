package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vuzeda.animewatchlist.tracker.module.localdatasource.Season as LocalSeason
import com.vuzeda.animewatchlist.tracker.module.localdatasource.SeasonLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.SeasonEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toLocalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
abstract class SeasonRoomDao : SeasonLocalDataSource {

    @Query("SELECT * FROM season ORDER BY animeId, orderIndex ASC")
    abstract fun observeAllEntities(): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM season WHERE animeId = :animeId ORDER BY orderIndex ASC")
    abstract fun observeByAnimeIdEntity(animeId: Long): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM season WHERE id = :id")
    abstract fun observeByIdEntity(id: Long): Flow<SeasonEntity?>

    @Query("SELECT * FROM season WHERE malId = :malId LIMIT 1")
    abstract suspend fun findByMalIdEntity(malId: Int): SeasonEntity?

    @Query("SELECT * FROM season WHERE animeId = :animeId ORDER BY orderIndex ASC")
    abstract suspend fun getByAnimeIdEntity(animeId: Long): List<SeasonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllEntities(seasons: List<SeasonEntity>)

    @Update
    abstract suspend fun updateEntity(season: SeasonEntity)

    override fun observeAll(): Flow<List<LocalSeason>> =
        observeAllEntities().map { it.map { e -> e.toLocalModel() } }

    override fun observeByAnimeId(animeId: Long): Flow<List<LocalSeason>> =
        observeByAnimeIdEntity(animeId).map { it.map { e -> e.toLocalModel() } }

    override fun observeById(id: Long): Flow<LocalSeason?> =
        observeByIdEntity(id).map { it?.toLocalModel() }

    override suspend fun findByMalId(malId: Int): LocalSeason? =
        findByMalIdEntity(malId)?.toLocalModel()

    override suspend fun getByAnimeId(animeId: Long): List<LocalSeason> =
        getByAnimeIdEntity(animeId).map { it.toLocalModel() }

    override suspend fun insertAll(seasons: List<LocalSeason>) =
        insertAllEntities(seasons.map { it.toEntity() })

    override suspend fun update(season: LocalSeason) = updateEntity(season.toEntity())

    @Query("UPDATE season SET lastCheckedAiredEpisodeCount = :count WHERE id = :seasonId")
    override abstract suspend fun updateNotificationData(seasonId: Long, count: Int?)

    @Query("UPDATE season SET isEpisodeNotificationsEnabled = :enabled WHERE id = :seasonId")
    override abstract suspend fun updateEpisodeNotificationsEnabled(seasonId: Long, enabled: Boolean)

    @Query("SELECT * FROM season WHERE isEpisodeNotificationsEnabled = 1")
    abstract suspend fun getSeasonsWithEpisodeNotificationsEntities(): List<SeasonEntity>

    @Query("SELECT malId FROM season")
    override abstract fun observeAllMalIds(): Flow<List<Int>>

    override suspend fun getSeasonsWithEpisodeNotifications(): List<LocalSeason> =
        getSeasonsWithEpisodeNotificationsEntities().map { it.toLocalModel() }
}
