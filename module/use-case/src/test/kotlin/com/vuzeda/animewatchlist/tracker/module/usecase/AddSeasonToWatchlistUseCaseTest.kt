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

class AddSeasonToWatchlistUseCaseTest {

    private val seasonRepository: SeasonRepository = mockk()
    private val useCase = AddSeasonToWatchlistUseCase(seasonRepository)

    @Test
    fun `marks season as in-watchlist with given status`() = runTest {
        val season = Season(id = 1L, animeId = 1L, malId = 100, title = "S1", isInWatchlist = false)
        val slot = slot<Season>()
        coJustRun { seasonRepository.updateSeason(capture(slot)) }

        useCase(season, WatchStatus.WATCHING)

        assertThat(slot.captured.isInWatchlist).isTrue()
        assertThat(slot.captured.status).isEqualTo(WatchStatus.WATCHING)
        assertThat(slot.captured.id).isEqualTo(1L)
        coVerify { seasonRepository.updateSeason(season.copy(isInWatchlist = true, status = WatchStatus.WATCHING)) }
    }
}
