package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveAllSeasonsUseCaseTest {

    private val repository = mockk<SeasonRepository>()
    private val useCase = ObserveAllSeasonsUseCase(repository)

    @Test
    fun `emits all seasons from repository`() = runTest {
        val seasons = listOf(
            Season(id = 1, malId = 100, title = "Season 1"),
            Season(id = 2, malId = 200, title = "Season 2")
        )
        every { repository.observeAllSeasons() } returns flowOf(seasons)

        useCase().test {
            val result = awaitItem()

            assertThat(result).hasSize(2)
            assertThat(result[0].title).isEqualTo("Season 1")
            assertThat(result[1].title).isEqualTo("Season 2")
            awaitComplete()
        }

        verify { repository.observeAllSeasons() }
    }

    @Test
    fun `emits empty list when no seasons exist`() = runTest {
        every { repository.observeAllSeasons() } returns flowOf(emptyList())

        useCase().test {
            val result = awaitItem()

            assertThat(result).isEmpty()
            awaitComplete()
        }
    }
}
