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

class ObserveSeasonsForAnimeUseCaseTest {

    private val seasonRepository: SeasonRepository = mockk()
    private val useCase = ObserveSeasonsForAnimeUseCase(seasonRepository)

    @Test
    fun `emits seasons for the given anime`() = runTest {
        val seasons = listOf(Season(malId = 1, title = "S1"), Season(malId = 2, title = "S2"))
        every { seasonRepository.observeSeasonsForAnime(3L) } returns flowOf(seasons)

        useCase(3L).test {
            assertThat(awaitItem()).isEqualTo(seasons)
            awaitComplete()
        }
    }
}
