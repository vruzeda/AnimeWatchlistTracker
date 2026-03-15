package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateSeasonStatusUseCaseTest {

    private val seasonRepository: SeasonRepository = mockk()
    private val useCase = UpdateSeasonStatusUseCase(seasonRepository)

    @Test
    fun `updates season with new status`() = runTest {
        val season = Season(id = 1L, malId = 100, title = "S1", status = WatchStatus.PLAN_TO_WATCH)
        val slot = slot<Season>()
        coJustRun { seasonRepository.updateSeason(capture(slot)) }

        useCase(season, WatchStatus.COMPLETED)

        assertThat(slot.captured.status).isEqualTo(WatchStatus.COMPLETED)
        assertThat(slot.captured.id).isEqualTo(1L)
        coVerify { seasonRepository.updateSeason(season.copy(status = WatchStatus.COMPLETED)) }
    }
}
