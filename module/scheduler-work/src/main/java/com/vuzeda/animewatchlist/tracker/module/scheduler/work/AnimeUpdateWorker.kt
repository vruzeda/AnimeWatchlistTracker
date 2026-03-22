package com.vuzeda.animewatchlist.tracker.module.scheduler.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.notification.AnimeUpdateNotifier
import com.vuzeda.animewatchlist.tracker.module.usecase.CheckAnimeUpdatesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RecordAnimeUpdateRunUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AnimeUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val animeUpdateNotifier: AnimeUpdateNotifier,
    private val checkAnimeUpdatesUseCase: CheckAnimeUpdatesUseCase,
    private val recordAnimeUpdateRunUseCase: RecordAnimeUpdateRunUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val updates = checkAnimeUpdatesUseCase()
            for (update in updates) {
                animeUpdateNotifier.showUpdateNotification(update)
            }
            recordAnimeUpdateRunUseCase()
            Result.success()
        } catch (e: Exception) {
            when (e) {
                is DataError.Network, is DataError.RateLimited -> Result.retry()
                else -> Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "anime_update_check"
        const val WORK_NAME_IMMEDIATE = "anime_update_check_immediate"
    }
}
