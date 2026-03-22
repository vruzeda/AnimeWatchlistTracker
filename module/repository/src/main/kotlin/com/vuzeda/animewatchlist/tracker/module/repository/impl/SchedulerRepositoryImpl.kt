package com.vuzeda.animewatchlist.tracker.module.repository.impl

import com.vuzeda.animewatchlist.tracker.module.localdatasource.SchedulerLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

class SchedulerRepositoryImpl @Inject constructor(
    private val animeUpdateScheduler: AnimeUpdateScheduler,
    private val localDataSource: SchedulerLocalDataSource,
    private val clock: Clock = Clock.System
) : SchedulerRepository {

    override fun schedulePeriodicAnimeUpdate() = animeUpdateScheduler.schedulePeriodicUpdate()

    override fun scheduleImmediateAnimeUpdate() = animeUpdateScheduler.scheduleImmediateUpdate()

    override fun observeLastAnimeUpdateRun(): Flow<Instant?> =
        localDataSource.observeLastAnimeUpdateRun().map { ms ->
            ms?.let { Instant.fromEpochMilliseconds(it) }
        }

    override suspend fun recordAnimeUpdateRun() {
        localDataSource.setLastAnimeUpdateRun(clock.now().toEpochMilliseconds())
    }
}
