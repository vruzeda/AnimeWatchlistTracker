package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RecordAnimeUpdateRunUseCaseTest {

    private val repository = mockk<SchedulerRepository>(relaxUnitFun = true)
    private val useCase = RecordAnimeUpdateRunUseCase(repository)

    @Test
    fun `delegates to repository recordAnimeUpdateRun`() = runTest {
        useCase()

        coVerify(exactly = 1) { repository.recordAnimeUpdateRun() }
    }
}
