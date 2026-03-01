package com.vuzeda.animewatchlist.tracker.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveSeasonByIdUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = ObserveSeasonByIdUseCase(repository)

    @Test
    fun `emits season when found`() = runTest {
        val season = Season(id = 1L, animeId = 1L, malId = 100, title = "Season 1")
        every { repository.observeSeasonById(1L) } returns flowOf(season)

        useCase(1L).test {
            val result = awaitItem()

            assertThat(result).isEqualTo(season)
            awaitComplete()
        }

        verify { repository.observeSeasonById(1L) }
    }

    @Test
    fun `emits null when season not found`() = runTest {
        every { repository.observeSeasonById(999L) } returns flowOf(null)

        useCase(999L).test {
            val result = awaitItem()

            assertThat(result).isNull()
            awaitComplete()
        }
    }
}
