package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RemoveAnimeByMalIdUseCaseTest {

    private val animeRepository: AnimeRepository = mockk()
    private val seasonRepository: SeasonRepository = mockk()
    private val useCase = RemoveAnimeByMalIdUseCase(animeRepository, seasonRepository)

    @Test
    fun `returns empty set and skips deletion when no anime owns the mal id`() = runTest {
        coEvery { seasonRepository.findAnimeIdBySeasonMalId(999) } returns null

        val result = useCase(999)

        assertThat(result).isEmpty()
        coVerify(exactly = 0) { animeRepository.deleteAnime(any()) }
    }

    @Test
    fun `deletes anime and returns all season mal ids`() = runTest {
        val seasons = listOf(
            Season(malId = 101, title = "S1"),
            Season(malId = 102, title = "S2"),
        )
        coEvery { seasonRepository.findAnimeIdBySeasonMalId(101) } returns 5L
        coEvery { seasonRepository.getSeasonsForAnime(5L) } returns seasons
        coJustRun { animeRepository.deleteAnime(5L) }

        val result = useCase(101)

        assertThat(result).containsExactly(101, 102)
        coVerify { animeRepository.deleteAnime(5L) }
    }
}
