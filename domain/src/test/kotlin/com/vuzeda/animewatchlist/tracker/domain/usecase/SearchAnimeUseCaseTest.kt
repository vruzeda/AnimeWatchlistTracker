package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SearchAnimeUseCaseTest {

    private val remoteRepository = mockk<AnimeRemoteRepository>()
    private val useCase = SearchAnimeUseCase(remoteRepository)

    @Test
    fun `returns search results on success`() = runTest {
        val results = listOf(
            SearchResult(malId = 20, title = "Naruto"),
            SearchResult(malId = 1735, title = "Naruto Shippuden")
        )
        coEvery { remoteRepository.searchAnime("Naruto") } returns Result.success(results)

        val result = useCase("Naruto")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(results)
        coVerify { remoteRepository.searchAnime("Naruto") }
    }

    @Test
    fun `returns failure when search fails`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { remoteRepository.searchAnime("test") } returns Result.failure(exception)

        val result = useCase("test")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}
