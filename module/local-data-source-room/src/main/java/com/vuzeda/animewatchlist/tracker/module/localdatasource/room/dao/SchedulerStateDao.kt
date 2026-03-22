package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.SchedulerStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchedulerStateDao {

    @Query("SELECT * FROM scheduler_state WHERE id = 1")
    fun observe(): Flow<SchedulerStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SchedulerStateEntity)
}
