package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SearchAnimeUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = SearchAnimeUseCase(repository)

    @Test
    fun `returns search results on success`() = runTest {
        val results = listOf(Anime(title = "Naruto"), Anime(title = "Naruto Shippuden"))
        coEvery { repository.searchAnime("Naruto") } returns Result.success(results)

        val result = useCase("Naruto")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(results)
        coVerify { repository.searchAnime("Naruto") }
    }

    @Test
    fun `returns failure when search fails`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { repository.searchAnime("test") } returns Result.failure(exception)

        val result = useCase("test")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}
