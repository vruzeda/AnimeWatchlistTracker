package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FetchEpisodesUseCaseTest {

    private val animeRepository = mockk<AnimeRepository>()
    private val useCase = FetchEpisodesUseCase(animeRepository)

    @Test
    fun `returns episode page on success`() = runTest {
        val episodes = listOf(
            EpisodeInfo(number = 1, title = "Ep 1", aired = "2023-01-01", isFiller = false, isRecap = false)
        )
        val page = EpisodePage(episodes = episodes, hasNextPage = true, nextPage = 2)
        coEvery { animeRepository.fetchAnimeEpisodes(malId = 100, page = 1) } returns Result.success(page)

        val result = useCase(malId = 100, page = 1)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.episodes).hasSize(1)
        assertThat(result.getOrNull()?.hasNextPage).isTrue()

        coVerify { animeRepository.fetchAnimeEpisodes(malId = 100, page = 1) }
    }

    @Test
    fun `returns failure on error`() = runTest {
        coEvery { animeRepository.fetchAnimeEpisodes(malId = 100, page = 1) } returns Result.failure(Exception("API error"))

        val result = useCase(malId = 100, page = 1)

        assertThat(result.isFailure).isTrue()
    }
}
