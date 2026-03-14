package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetSeasonAnimeUseCaseTest {

    private val remoteRepository = mockk<AnimeRemoteDataSource>()
    private val useCase = GetSeasonAnimeUseCase(remoteRepository)

    @Test
    fun `returns seasonal anime page on success`() = runTest {
        val page = SeasonalAnimePage(
            results = listOf(SearchResult(malId = 1, title = "Frieren")),
            hasNextPage = true,
            currentPage = 1
        )
        coEvery {
            remoteRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.WINTER,
                page = 1
            )
        } returns Result.success(page)

        val result = useCase(year = 2026, season = AnimeSeason.WINTER, page = 1)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(page)
        coVerify {
            remoteRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.WINTER,
                page = 1
            )
        }
    }

    @Test
    fun `returns failure when fetch fails`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery {
            remoteRepository.fetchSeasonAnime(
                year = 2025,
                season = AnimeSeason.FALL,
                page = 1
            )
        } returns Result.failure(exception)

        val result = useCase(year = 2025, season = AnimeSeason.FALL)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `defaults to page 1`() = runTest {
        coEvery {
            remoteRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.SPRING,
                page = 1
            )
        } returns Result.success(
            SeasonalAnimePage(results = emptyList(), hasNextPage = false, currentPage = 1)
        )

        useCase(year = 2026, season = AnimeSeason.SPRING)

        coVerify {
            remoteRepository.fetchSeasonAnime(
                year = 2026,
                season = AnimeSeason.SPRING,
                page = 1
            )
        }
    }
}
