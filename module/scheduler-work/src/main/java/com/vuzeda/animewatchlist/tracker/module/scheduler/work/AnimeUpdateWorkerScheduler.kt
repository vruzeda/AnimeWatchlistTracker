package com.vuzeda.animewatchlist.tracker.module.scheduler.work

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AnimeUpdateWorkerScheduler @Inject constructor(
    private val workManager: WorkManager
) : AnimeUpdateScheduler {

    override fun schedulePeriodicUpdate() {
        workManager.cancelUniqueWork(LEGACY_BACKFILL_WORK_NAME)
        workManager.cancelUniqueWork(LEGACY_DAYTIME_PERIODIC_WORK_NAME)
        workManager.enqueueUniquePeriodicWork(
            AnimeUpdateWorker.WORK_NAME_NIGHTLY,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<AnimeUpdateWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelayUntilNextNightRun(ZonedDateTime.now()))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresDeviceIdle(true)
                        .build()
                )
                .build()
        )
    }

    override fun scheduleImmediateUpdate() {
        workManager.enqueueUniqueWork(
            AnimeUpdateWorker.WORK_NAME_IMMEDIATE,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<AnimeUpdateWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
                .build()
        )
    }

    override fun scheduleRetryAfterRateLimit(delayMs: Long) {
        workManager.enqueueUniqueWork(
            AnimeUpdateWorker.WORK_NAME_RATE_LIMIT_RETRY,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<AnimeUpdateWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()
        )
    }

    companion object {
        const val LEGACY_BACKFILL_WORK_NAME = "backfill_airing_season"
        const val LEGACY_DAYTIME_PERIODIC_WORK_NAME = "anime_update_check"
        private val NIGHT_RUN_TIME: LocalTime = LocalTime.of(3, 0)

        fun initialDelayUntilNextNightRun(now: ZonedDateTime): Duration {
            val nightRunToday = now.toLocalDate().atTime(NIGHT_RUN_TIME).atZone(now.zone)
            val nextNightRun = if (now.isBefore(nightRunToday)) nightRunToday else nightRunToday.plusDays(1)
            return Duration.between(now, nextNightRun)
        }
    }
}
