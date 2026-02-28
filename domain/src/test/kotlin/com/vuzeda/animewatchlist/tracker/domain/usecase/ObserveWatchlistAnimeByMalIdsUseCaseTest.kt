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

class ObserveWatchlistAnimeByMalIdsUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = ObserveWatchlistAnimeByMalIdsUseCase(repository)

    @Test
    fun `emits anime matching given mal ids`() = runTest {
        val anime = listOf(
            Anime(id = 1L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING),
            Anime(id = 2L, malId = 30, title = "Naruto", status = WatchStatus.COMPLETED)
        )
        every { repository.observeAnimeByMalIds(listOf(21, 30, 99)) } returns flowOf(anime)

        useCase(listOf(21, 30, 99)).test {
            val result = awaitItem()

            assertThat(result).hasSize(2)
            assertThat(result[0].malId).isEqualTo(21)
            assertThat(result[1].malId).isEqualTo(30)
            awaitComplete()
        }

        verify { repository.observeAnimeByMalIds(listOf(21, 30, 99)) }
    }

    @Test
    fun `emits empty list when no matches found`() = runTest {
        every { repository.observeAnimeByMalIds(listOf(999)) } returns flowOf(emptyList())

        useCase(listOf(999)).test {
            val result = awaitItem()

            assertThat(result).isEmpty()
            awaitComplete()
        }
    }
}
