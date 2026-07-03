package com.vuzeda.animewatchlist.tracker.module.scheduler.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.notification.AnimeUpdateNotifier
import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import com.vuzeda.animewatchlist.tracker.module.usecase.CheckAnimeUpdatesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RecordAnimeUpdateAttemptUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

@HiltWorker
class AnimeUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val animeUpdateNotifier: AnimeUpdateNotifier,
    private val animeUpdateScheduler: AnimeUpdateScheduler,
    private val checkAnimeUpdatesUseCase: CheckAnimeUpdatesUseCase,
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase,
    private val recordAnimeUpdateAttemptUseCase: RecordAnimeUpdateAttemptUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val titleLanguage = observeTitleLanguageUseCase().first()
            val updates = checkAnimeUpdatesUseCase()
            for (update in updates) {
                animeUpdateNotifier.showUpdateNotification(update, titleLanguage)
            }
            recordAnimeUpdateAttemptUseCase(AnimeUpdateResult.Success)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            when (e) {
                is DataError.RateLimited -> {
                    recordAnimeUpdateAttemptUseCase(
                        AnimeUpdateResult.WillRetry(reason = e.message, retryCount = runAttemptCount)
                    )
                    val retryAfterMs = e.retryAfterMs
                    if (retryAfterMs != null) {
                        animeUpdateScheduler.scheduleRetryAfterRateLimit(retryAfterMs)
                        Result.success()
                    } else if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        recordAnimeUpdateAttemptUseCase(AnimeUpdateResult.Failure(e.message))
                        Result.failure()
                    }
                }
                is DataError.Network -> {
                    recordAnimeUpdateAttemptUseCase(
                        AnimeUpdateResult.WillRetry(reason = e.message, retryCount = runAttemptCount)
                    )
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        recordAnimeUpdateAttemptUseCase(AnimeUpdateResult.Failure(e.message))
                        Result.failure()
                    }
                }
                else -> {
                    recordAnimeUpdateAttemptUseCase(AnimeUpdateResult.Failure(e.message))
                    Result.failure()
                }
            }
        }
    }

    companion object {
        const val WORK_NAME = "anime_update_check"
        const val WORK_NAME_IMMEDIATE = "anime_update_check_immediate"
        const val WORK_NAME_RATE_LIMIT_RETRY = "anime_update_check_rate_limit_retry"
    }
}
