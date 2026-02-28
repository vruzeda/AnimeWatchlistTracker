package com.vuzeda.animewatchlist.tracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vuzeda.animewatchlist.tracker.domain.usecase.CheckAnimeUpdatesUseCase
import com.vuzeda.animewatchlist.tracker.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AnimeUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val checkAnimeUpdatesUseCase: CheckAnimeUpdatesUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val updates = checkAnimeUpdatesUseCase()
            for (update in updates) {
                notificationHelper.showUpdateNotification(update)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "anime_update_check"
    }
}
