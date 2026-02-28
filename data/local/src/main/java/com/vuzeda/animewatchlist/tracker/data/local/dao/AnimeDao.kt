package com.vuzeda.animewatchlist.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime ORDER BY title ASC")
    fun observeAll(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE status = :status ORDER BY title ASC")
    fun observeByStatus(status: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE id = :id")
    suspend fun getById(id: Long): AnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeEntity): Long

    @Update
    suspend fun update(anime: AnimeEntity)

    @Query("DELETE FROM anime WHERE id = :id")
    suspend fun deleteById(id: Long)
}
