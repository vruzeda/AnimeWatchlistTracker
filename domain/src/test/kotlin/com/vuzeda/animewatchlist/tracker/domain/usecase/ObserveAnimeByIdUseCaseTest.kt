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

class ObserveAnimeByIdUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = ObserveAnimeByIdUseCase(repository)

    @Test
    fun `emits anime when found`() = runTest {
        val anime = Anime(id = 1L, title = "Naruto", status = WatchStatus.WATCHING)
        every { repository.observeAnimeById(1L) } returns flowOf(anime)

        useCase(1L).test {
            val result = awaitItem()

            assertThat(result).isEqualTo(anime)
            awaitComplete()
        }

        verify { repository.observeAnimeById(1L) }
    }

    @Test
    fun `emits null when anime not found`() = runTest {
        every { repository.observeAnimeById(999L) } returns flowOf(null)

        useCase(999L).test {
            val result = awaitItem()

            assertThat(result).isNull()
            awaitComplete()
        }
    }
}
