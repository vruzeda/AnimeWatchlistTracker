package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SchedulerRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TriggerAnimeUpdateUseCaseTest {

    private val repository = mockk<SchedulerRepository>(relaxUnitFun = true)
    private val useCase = TriggerAnimeUpdateUseCase(repository)

    @Test
    fun `delegates to repository scheduleImmediateAnimeUpdate`() {
        useCase()

        verify(exactly = 1) { repository.scheduleImmediateAnimeUpdate() }
    }
}
