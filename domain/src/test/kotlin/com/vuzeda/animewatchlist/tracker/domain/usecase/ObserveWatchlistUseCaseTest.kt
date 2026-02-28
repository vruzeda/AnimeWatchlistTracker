package com.vuzeda.animewatchlist.tracker.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveWatchlistUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = ObserveWatchlistUseCase(repository)

    private val sampleAnimeList = listOf(
        Anime(id = 1, title = "Naruto", status = WatchStatus.WATCHING),
        Anime(id = 2, title = "One Piece", status = WatchStatus.PLAN_TO_WATCH)
    )

    @Test
    fun `returns full watchlist when no status filter is provided`() = runTest {
        every { repository.observeWatchlist() } returns flowOf(sampleAnimeList)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(sampleAnimeList)
            awaitComplete()
        }

        verify { repository.observeWatchlist() }
    }

    @Test
    fun `returns filtered watchlist when status is provided`() = runTest {
        val watchingList = sampleAnimeList.filter { it.status == WatchStatus.WATCHING }
        every { repository.observeWatchlistByStatus(WatchStatus.WATCHING) } returns flowOf(watchingList)

        useCase(WatchStatus.WATCHING).test {
            assertThat(awaitItem()).isEqualTo(watchingList)
            awaitComplete()
        }

        verify { repository.observeWatchlistByStatus(WatchStatus.WATCHING) }
    }
}
