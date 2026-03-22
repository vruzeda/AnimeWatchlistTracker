package com.vuzeda.animewatchlist.tracker.module.scheduler.work

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vuzeda.animewatchlist.tracker.module.scheduler.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SchedulerImpl @Inject constructor(
    private val workManager: WorkManager
) : Scheduler {

    override fun schedulePeriodicAnimeUpdate() {
        workManager.enqueueUniquePeriodicWork(
            AnimeUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<AnimeUpdateWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        )
    }

    override fun scheduleImmediateAnimeUpdate() {
        workManager.enqueueUniqueWork(
            AnimeUpdateWorker.WORK_NAME_IMMEDIATE,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<AnimeUpdateWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        )
    }
}
