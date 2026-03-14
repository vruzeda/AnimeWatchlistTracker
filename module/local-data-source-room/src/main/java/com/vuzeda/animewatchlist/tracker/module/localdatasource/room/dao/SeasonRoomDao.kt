package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.localdatasource.SeasonLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.SeasonEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toDomainModel
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toEntity
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

    override fun observeAll(): Flow<List<Season>> =
        observeAllEntities().map { it.map { e -> e.toDomainModel() } }

    override fun observeByAnimeId(animeId: Long): Flow<List<Season>> =
        observeByAnimeIdEntity(animeId).map { it.map { e -> e.toDomainModel() } }

    override fun observeById(id: Long): Flow<Season?> =
        observeByIdEntity(id).map { it?.toDomainModel() }

    override suspend fun findByMalId(malId: Int): Season? =
        findByMalIdEntity(malId)?.toDomainModel()

    override suspend fun getByAnimeId(animeId: Long): List<Season> =
        getByAnimeIdEntity(animeId).map { it.toDomainModel() }

    override suspend fun insertAll(seasons: List<Season>) =
        insertAllEntities(seasons.map { it.toEntity() })

    @Query("DELETE FROM season WHERE id = :id")
    override abstract suspend fun deleteById(id: Long)

    override suspend fun update(season: Season) = updateEntity(season.toEntity())

    @Query("UPDATE season SET lastCheckedAiredEpisodeCount = :count WHERE id = :seasonId")
    override abstract suspend fun updateNotificationData(seasonId: Long, count: Int?)

    @Query("UPDATE season SET isEpisodeNotificationsEnabled = :enabled WHERE id = :seasonId")
    override abstract suspend fun updateEpisodeNotificationsEnabled(seasonId: Long, enabled: Boolean)

    @Query("SELECT * FROM season WHERE isEpisodeNotificationsEnabled = 1")
    abstract suspend fun getSeasonsWithEpisodeNotificationsEntities(): List<SeasonEntity>

    @Query("SELECT malId FROM season")
    override abstract fun observeAllMalIds(): Flow<List<Int>>

    override suspend fun getSeasonsWithEpisodeNotifications(): List<Season> =
        getSeasonsWithEpisodeNotificationsEntities().map { it.toDomainModel() }
}
