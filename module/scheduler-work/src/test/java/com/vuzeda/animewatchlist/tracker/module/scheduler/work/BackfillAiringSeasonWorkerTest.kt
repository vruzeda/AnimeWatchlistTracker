package com.vuzeda.animewatchlist.tracker.module.scheduler.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.usecase.BackfillMissingAiringSeasonUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class BackfillAiringSeasonWorkerTest {

    private val backfillMissingAiringSeasonUseCase: BackfillMissingAiringSeasonUseCase = mockk()

    private fun createWorker(runAttemptCount: Int = 0): BackfillAiringSeasonWorker {
        val workerParams = mockk<WorkerParameters>(relaxed = true)
        every { workerParams.runAttemptCount } returns runAttemptCount
        return BackfillAiringSeasonWorker(
            appContext = mockk<Context>(relaxed = true),
            workerParams = workerParams,
            backfillMissingAiringSeasonUseCase = backfillMissingAiringSeasonUseCase
        )
    }

    @Test
    fun `doWork returns success when backfill completes`() = runTest {
        coJustRun { backfillMissingAiringSeasonUseCase() }

        val result = createWorker().doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
    }

    @Test
    fun `doWork retries on error when under attempt cap`() = runTest {
        coEvery { backfillMissingAiringSeasonUseCase() } throws IllegalStateException("backfill failed")

        val result = createWorker(runAttemptCount = 2).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Retry::class.java)
    }

    @Test
    fun `doWork fails on error when attempt cap is reached`() = runTest {
        coEvery { backfillMissingAiringSeasonUseCase() } throws IllegalStateException("backfill failed")

        val result = createWorker(runAttemptCount = 3).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Failure::class.java)
    }

    @Test
    fun `doWork rethrows CancellationException instead of retrying`() = runTest {
        coEvery { backfillMissingAiringSeasonUseCase() } throws CancellationException("cancelled")

        val thrown = runCatching { createWorker().doWork() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(CancellationException::class.java)
    }
}
