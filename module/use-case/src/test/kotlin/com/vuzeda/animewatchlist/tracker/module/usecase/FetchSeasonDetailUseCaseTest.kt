package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FetchSeasonDetailUseCaseTest {

    private val animeRepository: AnimeRepository = mockk()
    private val useCase = FetchSeasonDetailUseCase(animeRepository)

    private val details = AnimeFullDetails(
        malId = 21,
        title = "One Piece",
        type = "TV",
        episodes = null,
        sequels = emptyList(),
    )

    @Test
    fun `returns success result from repository`() = runTest {
        coEvery { animeRepository.fetchAnimeFullById(21) } returns Result.success(details)

        val result = useCase(21)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(details)
    }

    @Test
    fun `returns failure result from repository`() = runTest {
        val error = RuntimeException("network error")
        coEvery { animeRepository.fetchAnimeFullById(21) } returns Result.failure(error)

        val result = useCase(21)

        assertThat(result.isFailure).isTrue()
    }
}
