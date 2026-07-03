package com.vuzeda.animewatchlist.tracker.module.scheduler.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.notification.AnimeUpdateNotifier
import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import com.vuzeda.animewatchlist.tracker.module.usecase.CheckAnimeUpdatesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RecordAnimeUpdateAttemptUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AnimeUpdateWorkerTest {

    private val animeUpdateNotifier: AnimeUpdateNotifier = mockk(relaxed = true)
    private val animeUpdateScheduler: AnimeUpdateScheduler = mockk(relaxed = true)
    private val checkAnimeUpdatesUseCase: CheckAnimeUpdatesUseCase = mockk()
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase = mockk()
    private val recordAnimeUpdateAttemptUseCase: RecordAnimeUpdateAttemptUseCase = mockk(relaxed = true)

    private fun createWorker(runAttemptCount: Int = 0): AnimeUpdateWorker {
        val workerParams = mockk<WorkerParameters>(relaxed = true)
        every { workerParams.runAttemptCount } returns runAttemptCount
        return AnimeUpdateWorker(
            appContext = mockk<Context>(relaxed = true),
            workerParams = workerParams,
            animeUpdateNotifier = animeUpdateNotifier,
            animeUpdateScheduler = animeUpdateScheduler,
            checkAnimeUpdatesUseCase = checkAnimeUpdatesUseCase,
            observeTitleLanguageUseCase = observeTitleLanguageUseCase,
            recordAnimeUpdateAttemptUseCase = recordAnimeUpdateAttemptUseCase
        )
    }

    @Test
    fun `doWork notifies each update and records success`() = runTest {
        val update = mockk<AnimeUpdate>()
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.ENGLISH)
        coEvery { checkAnimeUpdatesUseCase() } returns listOf(update)

        val result = createWorker().doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
        verify { animeUpdateNotifier.showUpdateNotification(update, TitleLanguage.ENGLISH) }
        coVerify { recordAnimeUpdateAttemptUseCase(AnimeUpdateResult.Success) }
    }

    @Test
    fun `doWork schedules rate limit retry and succeeds when retryAfterMs is provided`() = runTest {
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.ENGLISH)
        coEvery { checkAnimeUpdatesUseCase() } throws DataError.RateLimited(retryAfterMs = 3_600_000)

        val result = createWorker().doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
        verify { animeUpdateScheduler.scheduleRetryAfterRateLimit(3_600_000) }
        coVerify { recordAnimeUpdateAttemptUseCase(ofType<AnimeUpdateResult.WillRetry>()) }
    }

    @Test
    fun `doWork retries on rate limit without retryAfterMs when under attempt cap`() = runTest {
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.ENGLISH)
        coEvery { checkAnimeUpdatesUseCase() } throws DataError.RateLimited(retryAfterMs = null)

        val result = createWorker(runAttemptCount = 2).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Retry::class.java)
    }

    @Test
    fun `doWork fails on rate limit without retryAfterMs when attempt cap is reached`() = runTest {
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.ENGLISH)
        coEvery { checkAnimeUpdatesUseCase() } throws DataError.RateLimited(retryAfterMs = null)

        val result = createWorker(runAttemptCount = 3).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Failure::class.java)
        coVerify { recordAnimeUpdateAttemptUseCase(ofType<AnimeUpdateResult.Failure>()) }
    }

    @Test
    fun `doWork retries on network error when under attempt cap`() = runTest {
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.ENGLISH)
        coEvery { checkAnimeUpdatesUseCase() } throws DataError.Network(throwable = IOException("offline"))

        val result = createWorker(runAttemptCount = 0).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Retry::class.java)
        coVerify { recordAnimeUpdateAttemptUseCase(ofType<AnimeUpdateResult.WillRetry>()) }
    }

    @Test
    fun `doWork fails on network error when attempt cap is reached`() = runTest {
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.ENGLISH)
        coEvery { checkAnimeUpdatesUseCase() } throws DataError.Network(throwable = IOException("offline"))

        val result = createWorker(runAttemptCount = 3).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Failure::class.java)
        coVerify { recordAnimeUpdateAttemptUseCase(ofType<AnimeUpdateResult.Failure>()) }
    }

    @Test
    fun `doWork fails without retrying on unexpected errors`() = runTest {
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.ENGLISH)
        coEvery { checkAnimeUpdatesUseCase() } throws IllegalStateException("boom")

        val result = createWorker().doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Failure::class.java)
        coVerify { recordAnimeUpdateAttemptUseCase(ofType<AnimeUpdateResult.Failure>()) }
    }

    @Test
    fun `doWork rethrows CancellationException without recording an attempt`() = runTest {
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.ENGLISH)
        coEvery { checkAnimeUpdatesUseCase() } throws CancellationException("cancelled")

        val thrown = runCatching { createWorker().doWork() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(CancellationException::class.java)
        coVerify(exactly = 0) { recordAnimeUpdateAttemptUseCase(any()) }
    }
}
