package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveAnimeListUseCaseTest {

    private val animeRepository: AnimeRepository = mockk()
    private val useCase = ObserveAnimeListUseCase(animeRepository)

    private val anime = Anime(title = "Naruto", imageUrl = "url")

    @Test
    fun `delegates to observeAll when no status filter is given`() = runTest {
        every { animeRepository.observeAll() } returns flowOf(listOf(anime))

        useCase().test {
            assertThat(awaitItem()).containsExactly(anime)
            awaitComplete()
        }

        verify { animeRepository.observeAll() }
    }

    @Test
    fun `delegates to observeByStatus when status filter is given`() = runTest {
        every { animeRepository.observeByStatus(WatchStatus.WATCHING) } returns flowOf(listOf(anime))

        useCase(WatchStatus.WATCHING).test {
            assertThat(awaitItem()).containsExactly(anime)
            awaitComplete()
        }

        verify { animeRepository.observeByStatus(WatchStatus.WATCHING) }
    }
}
