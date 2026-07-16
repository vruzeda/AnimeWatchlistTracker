package com.vuzeda.animewatchlist.tracker.module.scheduler.work

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class AnimeUpdateWorkerSchedulerTest {

    private val workManager: WorkManager = mockk(relaxed = true)
    private val scheduler = AnimeUpdateWorkerScheduler(workManager)

    @Test
    fun `schedulePeriodicUpdate cancels legacy backfill and daytime periodic work before enqueueing`() {
        scheduler.schedulePeriodicUpdate()

        verifyOrder {
            workManager.cancelUniqueWork(AnimeUpdateWorkerScheduler.LEGACY_BACKFILL_WORK_NAME)
            workManager.cancelUniqueWork(AnimeUpdateWorkerScheduler.LEGACY_DAYTIME_PERIODIC_WORK_NAME)
            workManager.enqueueUniquePeriodicWork(
                AnimeUpdateWorker.WORK_NAME_NIGHTLY,
                ExistingPeriodicWorkPolicy.KEEP,
                any<PeriodicWorkRequest>()
            )
        }
    }

    @Test
    fun `scheduleImmediateUpdate enqueues one-time work without cancelling anything`() {
        scheduler.scheduleImmediateUpdate()

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                AnimeUpdateWorker.WORK_NAME_IMMEDIATE,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
        verify(exactly = 0) { workManager.cancelUniqueWork(any()) }
    }

    @Test
    fun `scheduleRetryAfterRateLimit replaces existing retry work`() {
        scheduler.scheduleRetryAfterRateLimit(delayMs = 5_000L)

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                AnimeUpdateWorker.WORK_NAME_RATE_LIMIT_RETRY,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `initialDelayUntilNextNightRun before three am targets same day`() {
        val now = ZonedDateTime.of(2026, 7, 8, 1, 30, 0, 0, ZoneId.of("Europe/Paris"))

        val delay = AnimeUpdateWorkerScheduler.initialDelayUntilNextNightRun(now)

        assertThat(delay).isEqualTo(Duration.ofMinutes(90))
    }

    @Test
    fun `initialDelayUntilNextNightRun at exactly three am targets next day`() {
        val now = ZonedDateTime.of(2026, 7, 8, 3, 0, 0, 0, ZoneId.of("Europe/Paris"))

        val delay = AnimeUpdateWorkerScheduler.initialDelayUntilNextNightRun(now)

        assertThat(delay).isEqualTo(Duration.ofHours(24))
    }

    @Test
    fun `initialDelayUntilNextNightRun after three am targets next day`() {
        val now = ZonedDateTime.of(2026, 7, 8, 21, 0, 0, 0, ZoneId.of("America/Sao_Paulo"))

        val delay = AnimeUpdateWorkerScheduler.initialDelayUntilNextNightRun(now)

        assertThat(delay).isEqualTo(Duration.ofHours(6))
    }

    @Test
    fun `initialDelayUntilNextNightRun is always positive and at most a day`() {
        val midnight = ZonedDateTime.of(2026, 7, 8, 0, 0, 0, 0, ZoneId.of("UTC"))
        val almostMidnight = ZonedDateTime.of(2026, 7, 8, 23, 59, 59, 0, ZoneId.of("UTC"))

        val delayFromMidnight = AnimeUpdateWorkerScheduler.initialDelayUntilNextNightRun(midnight)
        val delayFromAlmostMidnight = AnimeUpdateWorkerScheduler.initialDelayUntilNextNightRun(almostMidnight)

        assertThat(delayFromMidnight).isEqualTo(Duration.ofHours(3))
        assertThat(delayFromAlmostMidnight).isEqualTo(Duration.ofHours(3).plusSeconds(1))
    }
}
