package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
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

    @Query("UPDATE anime SET latestKnownSeasonStartDate = :date WHERE id = :animeId")
    override abstract suspend fun updateLatestKnownSeasonStartDate(animeId: Long, date: LocalDate)

    @Query("UPDATE anime SET lastSeasonCheckPerformedDate = :date WHERE id = :animeId")
    override abstract suspend fun updateLastSeasonCheckPerformedDate(animeId: Long, date: LocalDate)

    @Query("SELECT lastAnimeUpdateRunAt FROM scheduler_state WHERE id = 1")
    override abstract fun observeLastAnimeUpdateRun(): Flow<Long?>

    @Query("SELECT lastAnimeUpdateAttemptAt FROM scheduler_state WHERE id = 1")
    override abstract fun observeLastAnimeUpdateAttemptAt(): Flow<Long?>

    @Query("SELECT lastAnimeUpdateAttemptResult FROM scheduler_state WHERE id = 1")
    override abstract fun observeLastAnimeUpdateAttemptResult(): Flow<String?>

    @Query("SELECT lastAnimeUpdateAttemptFailureReason FROM scheduler_state WHERE id = 1")
    override abstract fun observeLastAnimeUpdateAttemptFailureReason(): Flow<String?>

    @Query("SELECT lastAnimeUpdateAttemptRetryCount FROM scheduler_state WHERE id = 1")
    override abstract fun observeLastAnimeUpdateAttemptRetryCount(): Flow<Int?>

    @Query("SELECT * FROM scheduler_state WHERE id = 1")
    protected abstract suspend fun getSchedulerStateEntity(): AnimeUpdateSchedulerStateEntity?

    @Transaction
    override suspend fun setLastAnimeUpdateRun(epochMillis: Long) {
        val current = getSchedulerStateEntity()
        upsertAnimeUpdateSchedulerState(
            AnimeUpdateSchedulerStateEntity(
                lastAnimeUpdateRunAt = epochMillis,
                lastAnimeUpdateAttemptAt = current?.lastAnimeUpdateAttemptAt,
                lastAnimeUpdateAttemptResult = current?.lastAnimeUpdateAttemptResult,
                lastAnimeUpdateAttemptFailureReason = current?.lastAnimeUpdateAttemptFailureReason,
                lastAnimeUpdateAttemptRetryCount = current?.lastAnimeUpdateAttemptRetryCount
            )
        )
    }

    @Transaction
    override suspend fun recordAnimeUpdateAttempt(epochMillis: Long, result: AnimeUpdateResult) {
        val current = getSchedulerStateEntity()
        val newLastRunAt = if (result is AnimeUpdateResult.Success) epochMillis else current?.lastAnimeUpdateRunAt
        upsertAnimeUpdateSchedulerState(
            AnimeUpdateSchedulerStateEntity(
                lastAnimeUpdateRunAt = newLastRunAt,
                lastAnimeUpdateAttemptAt = epochMillis,
                lastAnimeUpdateAttemptResult = result.toDbString(),
                lastAnimeUpdateAttemptFailureReason = when (result) {
                    is AnimeUpdateResult.Failure -> result.reason
                    is AnimeUpdateResult.WillRetry -> result.reason
                    else -> null
                },
                lastAnimeUpdateAttemptRetryCount = (result as? AnimeUpdateResult.WillRetry)?.retryCount
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertAnimeUpdateSchedulerState(entity: AnimeUpdateSchedulerStateEntity)
}

private fun AnimeUpdateResult.toDbString() = when (this) {
    is AnimeUpdateResult.Success -> "SUCCESS"
    is AnimeUpdateResult.Failure -> "FAILURE"
    is AnimeUpdateResult.WillRetry -> "WILL_RETRY"
}
