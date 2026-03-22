package com.vuzeda.animewatchlist.tracker.module.scheduler.work

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AnimeUpdateWorkerScheduler @Inject constructor(
    private val workManager: WorkManager
) : AnimeUpdateScheduler {

    override fun schedulePeriodicUpdate() {
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
                .build()
        )
    }
}
