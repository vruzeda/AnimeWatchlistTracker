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
    fun `emits only watchlist seasons with a known broadcast day`() = runTest {
        val seasons = listOf(
            Season(id = 1, malId = 100, title = "Airing Show", broadcastDay = "Saturdays", isInWatchlist = true),
            Season(id = 2, malId = 200, title = "No Broadcast Day", broadcastDay = null, isInWatchlist = true),
            Season(id = 3, malId = 300, title = "Not In Watchlist", broadcastDay = "Mondays", isInWatchlist = false)
        )
        every { repository.observeAllSeasons() } returns flowOf(seasons)

        useCase().test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("Airing Show")
            awaitComplete()
        }
    }

    @Test
    fun `emits empty list when no seasons have a broadcast day`() = runTest {
        val seasons = listOf(
            Season(id = 1, malId = 100, title = "Season A", broadcastDay = null, isInWatchlist = true)
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
    fun `excludes seasons not in watchlist even if they have a broadcast day`() = runTest {
        val seasons = listOf(
            Season(id = 1, malId = 100, title = "Non-watchlist with day", broadcastDay = "Fridays", isInWatchlist = false)
        )
        every { repository.observeAllSeasons() } returns flowOf(seasons)

        useCase().test {
            val result = awaitItem()

            assertThat(result).isEmpty()
            awaitComplete()
        }
    }
}
