package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateSeasonUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = UpdateSeasonUseCase(repository)

    @Test
    fun `delegates season update to repository`() = runTest {
        val season = Season(id = 1L, animeId = 5L, malId = 100, title = "Season 1", orderIndex = 2)
        coEvery { repository.updateSeason(season) } returns Unit

        useCase(season)

        coVerify { repository.updateSeason(season) }
    }
}
