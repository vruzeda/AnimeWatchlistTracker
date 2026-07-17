package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveScheduleUseCaseTest {

    private val repository = mockk<SeasonRepository>()
    private val useCase = ObserveScheduleUseCase(repository)

    @Test
    fun `emits only watchlist seasons with a known airing season`() = runTest {
        val seasons = listOf(
            Season(id = 1, malId = 100, title = "Airing Show", airingSeasonYear = 2026, airingSeasonName = "summer", broadcastDay = "Saturdays", isInWatchlist = true),
            Season(id = 2, malId = 200, title = "No Broadcast Day", airingSeasonYear = 2026, airingSeasonName = "fall", broadcastDay = null, isInWatchlist = true),
            Season(id = 3, malId = 200, title = "No Airing Season", airingSeasonYear = null, airingSeasonName = null, broadcastDay = null, isInWatchlist = true),
            Season(id = 4, malId = 300, title = "Not In Watchlist", airingSeasonYear = 2026, airingSeasonName = "summer", broadcastDay = "Mondays", isInWatchlist = false)
        )
        every { repository.observeAllSeasons() } returns flowOf(seasons)

        useCase().test {
            val result = awaitItem()

            assertThat(result).hasSize(2)
            assertThat(result[0].title).isEqualTo("Airing Show")
            assertThat(result[1].title).isEqualTo("No Broadcast Day")
            awaitComplete()
        }
    }

    @Test
    fun `emits empty list when no seasons have an airing season`() = runTest {
        val seasons = listOf(
            Season(id = 1, malId = 100, title = "Season A", airingSeasonYear = null, airingSeasonName = null, isInWatchlist = true)
        )
        every { repository.observeAllSeasons() } returns flowOf(seasons)

        useCase().test {
            val result = awaitItem()

            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `emits empty list when watchlist is empty`() = runTest {
        every { repository.observeAllSeasons() } returns flowOf(emptyList())

        useCase().test {
            val result = awaitItem()

            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `excludes seasons not in watchlist even if they have an airing season`() = runTest {
        val seasons = listOf(
            Season(id = 1, malId = 100, title = "Non-watchlist with airing season", airingSeasonYear = 2026, airingSeasonName = "summer", broadcastDay = "Fridays", isInWatchlist = false)
        )
        every { repository.observeAllSeasons() } returns flowOf(seasons)

        useCase().test {
            val result = awaitItem()

            assertThat(result).isEmpty()
            awaitComplete()
        }
    }
}
