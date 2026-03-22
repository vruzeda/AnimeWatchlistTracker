package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateSeasonProgressUseCaseTest {

    private val seasonRepository: SeasonRepository = mockk()
    private val useCase = UpdateSeasonProgressUseCase(seasonRepository)

    @Test
    fun `updates season with new current episode`() = runTest {
        val season = Season(id = 1L, malId = 100, title = "S1", currentEpisode = 3)
        val slot = slot<Season>()
        coJustRun { seasonRepository.updateSeason(capture(slot)) }

        useCase(season, 7)

        assertThat(slot.captured.currentEpisode).isEqualTo(7)
        assertThat(slot.captured.id).isEqualTo(1L)
        coVerify { seasonRepository.updateSeason(season.copy(currentEpisode = 7)) }
    }
}
