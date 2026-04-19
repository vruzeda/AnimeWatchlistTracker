package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchOrderBy
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.SearchFilterState
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SearchAnimeUseCaseTest {

    private val animeRepository = mockk<AnimeRepository>()
    private val useCase = SearchAnimeUseCase(animeRepository)

    @Test
    fun `returns search results on success`() = runTest {
        val results = listOf(
            SearchResult(malId = 20, title = "Naruto"),
            SearchResult(malId = 1735, title = "Naruto Shippuden")
        )
        coEvery {
            animeRepository.searchAnime("Naruto", SearchFilterState())
        } returns Result.success(results)

        val result = useCase("Naruto")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(results)
        coVerify { animeRepository.searchAnime("Naruto", SearchFilterState()) }
    }

    @Test
    fun `returns failure when search fails`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { animeRepository.searchAnime("test", any()) } returns Result.failure(exception)

        val result = useCase("test")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `passes filterState params to repository`() = runTest {
        val filterState = SearchFilterState(
            type = AnimeSearchType.MOVIE,
            status = AnimeSearchStatus.AIRING,
            orderBy = AnimeSearchOrderBy.SCORE,
            isAscending = false
        )
        val results = listOf(SearchResult(malId = 1, title = "Test"))
        coEvery { animeRepository.searchAnime("test", filterState) } returns Result.success(results)

        val result = useCase("test", filterState)

        assertThat(result.isSuccess).isTrue()
        coVerify { animeRepository.searchAnime("test", filterState) }
    }
}
