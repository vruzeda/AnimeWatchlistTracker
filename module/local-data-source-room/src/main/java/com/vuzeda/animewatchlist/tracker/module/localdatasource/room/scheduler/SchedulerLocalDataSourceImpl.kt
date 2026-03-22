package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.scheduler

import com.vuzeda.animewatchlist.tracker.module.localdatasource.SchedulerLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.SchedulerStateDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.SchedulerStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SchedulerLocalDataSourceImpl(
    private val dao: SchedulerStateDao
) : SchedulerLocalDataSource {

    override fun observeLastAnimeUpdateRun(): Flow<Long?> =
        dao.observe().map { it?.lastAnimeUpdateRunAt }

    override suspend fun setLastAnimeUpdateRun(epochMillis: Long) {
        dao.upsert(SchedulerStateEntity(lastAnimeUpdateRunAt = epochMillis))
    }
}
