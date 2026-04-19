package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetSeasonAnimeUseCaseTest {

    private val animeRepository = mockk<AnimeRepository>()
    private val useCase = GetSeasonAnimeUseCase(animeRepository)

    @Test
    fun `returns seasonal anime page on success`() = runTest {
        val page = SeasonalAnimePage(
            results = listOf(SearchResult(malId = 1, title = "Frieren")),
            hasNextPage = true,
            currentPage = 1
        )
        coEvery {
            animeRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.WINTER,
                page = 1,
                filter = AnimeSearchType.ALL
            )
        } returns Result.success(page)

        val result = useCase(year = 2026, season = AnimeSeason.WINTER, page = 1)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(page)
        coVerify {
            animeRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.WINTER,
                page = 1,
                filter = AnimeSearchType.ALL
            )
        }
    }

    @Test
    fun `returns failure when fetch fails`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery {
            animeRepository.fetchSeasonAnime(
                year = 2025,
                season = AnimeSeason.FALL,
                page = 1,
                filter = AnimeSearchType.ALL
            )
        } returns Result.failure(exception)

        val result = useCase(year = 2025, season = AnimeSeason.FALL)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `defaults to page 1`() = runTest {
        coEvery {
            animeRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.SPRING,
                page = 1,
                filter = AnimeSearchType.ALL
            )
        } returns Result.success(
            SeasonalAnimePage(results = emptyList(), hasNextPage = false, currentPage = 1)
        )

        useCase(year = 2026, season = AnimeSeason.SPRING)

        coVerify {
            animeRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.SPRING,
                page = 1,
                filter = AnimeSearchType.ALL
            )
        }
    }

    @Test
    fun `passes filter param to repository`() = runTest {
        val page = SeasonalAnimePage(
            results = listOf(SearchResult(malId = 5, title = "Movie Anime")),
            hasNextPage = false,
            currentPage = 1
        )
        coEvery {
            animeRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.SPRING,
                page = 1,
                filter = AnimeSearchType.MOVIE
            )
        } returns Result.success(page)

        val result = useCase(year = 2026, season = AnimeSeason.SPRING, filter = AnimeSearchType.MOVIE)

        assertThat(result.isSuccess).isTrue()
        coVerify {
            animeRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.SPRING,
                page = 1,
                filter = AnimeSearchType.MOVIE
            )
        }
    }
}
