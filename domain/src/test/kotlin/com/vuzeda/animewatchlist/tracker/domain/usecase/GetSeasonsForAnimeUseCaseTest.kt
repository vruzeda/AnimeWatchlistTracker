package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetSeasonsForAnimeUseCaseTest {

    private val repository = mockk<SeasonRepository>()
    private val useCase = GetSeasonsForAnimeUseCase(repository)

    @Test
    fun `returns seasons from repository`() = runTest {
        val seasons = listOf(
            Season(id = 1L, animeId = 5L, malId = 100, title = "Season 1"),
            Season(id = 2L, animeId = 5L, malId = 101, title = "Season 2")
        )
        coEvery { repository.getSeasonsForAnime(5L) } returns seasons

        val result = useCase(5L)

        assertThat(result).isEqualTo(seasons)
    }
}
