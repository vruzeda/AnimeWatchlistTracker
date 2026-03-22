package com.vuzeda.animewatchlist.tracker.module.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.localdatasource.SchedulerLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class AnimeUpdateSchedulerRepositoryImplTest {

    private val animeUpdateScheduler: AnimeUpdateScheduler = mockk(relaxUnitFun = true)
    private val localDataSource: SchedulerLocalDataSource = mockk(relaxUnitFun = true)
    private val fixedInstant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    private val clock: Clock = mockk {
        every { now() } returns fixedInstant
    }
    private val repository = SchedulerRepositoryImpl(animeUpdateScheduler, localDataSource, clock)

    @Test
    fun `schedulePeriodicAnimeUpdate delegates to Scheduler`() {
        repository.schedulePeriodicAnimeUpdate()

        verify(exactly = 1) { animeUpdateScheduler.schedulePeriodicUpdate() }
    }

    @Test
    fun `scheduleImmediateAnimeUpdate delegates to Scheduler`() {
        repository.scheduleImmediateAnimeUpdate()

        verify(exactly = 1) { animeUpdateScheduler.scheduleImmediateUpdate() }
    }

    @Test
    fun `observeLastAnimeUpdateRun maps null Long to null Instant`() = runTest {
        every { localDataSource.observeLastAnimeUpdateRun() } returns flowOf(null)

        repository.observeLastAnimeUpdateRun().test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `observeLastAnimeUpdateRun maps epochMillis to Instant`() = runTest {
        val epochMillis = 1_700_000_000_000L
        every { localDataSource.observeLastAnimeUpdateRun() } returns flowOf(epochMillis)

        repository.observeLastAnimeUpdateRun().test {
            assertThat(awaitItem()).isEqualTo(Instant.fromEpochMilliseconds(epochMillis))
            awaitComplete()
        }
    }

    @Test
    fun `recordAnimeUpdateRun writes current clock time to local data source`() = runTest {
        coEvery { localDataSource.setLastAnimeUpdateRun(any()) } returns Unit

        repository.recordAnimeUpdateRun()

        coVerify(exactly = 1) { localDataSource.setLastAnimeUpdateRun(fixedInstant.toEpochMilliseconds()) }
    }
}
