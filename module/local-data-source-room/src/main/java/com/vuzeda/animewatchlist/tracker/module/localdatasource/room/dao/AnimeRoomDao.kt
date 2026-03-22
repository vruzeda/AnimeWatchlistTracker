package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.localdatasource.AnimeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.AnimeUpdateSchedulerStateEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toDomainModel
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@Dao
abstract class AnimeRoomDao : AnimeLocalDataSource {

    @Query("SELECT * FROM anime ORDER BY title ASC")
    abstract fun observeAllEntities(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE id = :id")
    abstract fun observeByIdEntity(id: Long): Flow<AnimeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEntity(anime: AnimeEntity): Long

    @Update
    abstract suspend fun updateEntity(anime: AnimeEntity)

    @Query("SELECT * FROM anime WHERE id = :id")
    abstract suspend fun getByIdEntity(id: Long): AnimeEntity?

    @Query("SELECT * FROM anime WHERE notificationType != 'NONE'")
    abstract suspend fun getNotificationEnabledAnimeEntities(): List<AnimeEntity>

    @Query("UPDATE anime SET notificationType = :notificationType WHERE id = :id")
    abstract suspend fun updateNotificationTypeByName(id: Long, notificationType: String)

    override fun observeAll(): Flow<List<Anime>> =
        observeAllEntities().map { it.map { e -> e.toDomainModel() } }

    override fun observeById(id: Long): Flow<Anime?> =
        observeByIdEntity(id).map { it?.toDomainModel() }

    override suspend fun insert(anime: Anime): Long = insertEntity(anime.toEntity())

    override suspend fun update(anime: Anime) = updateEntity(anime.toEntity())

    @Query("DELETE FROM anime WHERE id = :id")
    override abstract suspend fun deleteById(id: Long)

    @Query("DELETE FROM anime")
    override abstract suspend fun deleteAll()

    override suspend fun getById(id: Long): Anime? = getByIdEntity(id)?.toDomainModel()

    override suspend fun getNotificationEnabledAnime(): List<Anime> =
        getNotificationEnabledAnimeEntities().map { it.toDomainModel() }

    override suspend fun updateNotificationType(id: Long, notificationType: NotificationType) =
        updateNotificationTypeByName(id, notificationType.name)

    @Query("UPDATE anime SET lastSeasonCheckDate = :date WHERE id = :animeId")
    override abstract suspend fun updateLastSeasonCheckDate(animeId: Long, date: LocalDate)

    @Query("SELECT lastAnimeUpdateRunAt FROM scheduler_state WHERE id = 1")
    override abstract fun observeLastAnimeUpdateRun(): Flow<Long?>

    override suspend fun setLastAnimeUpdateRun(epochMillis: Long) {
        upsertAnimeUpdateSchedulerState(AnimeUpdateSchedulerStateEntity(lastAnimeUpdateRunAt = epochMillis))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertAnimeUpdateSchedulerState(entity: AnimeUpdateSchedulerStateEntity)
}
