package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vuzeda.animewatchlist.tracker.module.localdatasource.Anime as LocalAnime
import com.vuzeda.animewatchlist.tracker.module.localdatasource.AnimeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toLocalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
abstract class AnimeRoomDao : AnimeLocalDataSource {

    @Query("SELECT * FROM anime ORDER BY title ASC")
    abstract fun observeAllEntities(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE status = :status ORDER BY title ASC")
    abstract fun observeByStatusEntity(status: String): Flow<List<AnimeEntity>>

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

    override fun observeAll(): Flow<List<LocalAnime>> =
        observeAllEntities().map { it.map { e -> e.toLocalModel() } }

    override fun observeByStatus(status: String): Flow<List<LocalAnime>> =
        observeByStatusEntity(status).map { it.map { e -> e.toLocalModel() } }

    override fun observeById(id: Long): Flow<LocalAnime?> =
        observeByIdEntity(id).map { it?.toLocalModel() }

    override suspend fun insert(anime: LocalAnime): Long = insertEntity(anime.toEntity())

    override suspend fun update(anime: LocalAnime) = updateEntity(anime.toEntity())

    @Query("DELETE FROM anime WHERE id = :id")
    override abstract suspend fun deleteById(id: Long)

    @Query("DELETE FROM anime")
    override abstract suspend fun deleteAll()

    override suspend fun getById(id: Long): LocalAnime? = getByIdEntity(id)?.toLocalModel()

    override suspend fun getNotificationEnabledAnime(): List<LocalAnime> =
        getNotificationEnabledAnimeEntities().map { it.toLocalModel() }

    @Query("UPDATE anime SET notificationType = :notificationType WHERE id = :id")
    override abstract suspend fun updateNotificationType(id: Long, notificationType: String)
}
