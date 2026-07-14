package com.vuzeda.animewatchlist.tracker.module.repository.impl

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeProvider
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProviderSwitchingAnimeRemoteDataSourceTest {

    private val jikanDataSource: AnimeRemoteDataSource = mockk()
    private val malDataSource: AnimeRemoteDataSource = mockk()
    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val dataSource = ProviderSwitchingAnimeRemoteDataSource(
        jikanDataSource = jikanDataSource,
        malDataSource = malDataSource,
        userPreferencesRepository = userPreferencesRepository
    )

    private fun selectProvider(provider: AnimeProvider) {
        every { userPreferencesRepository.observeAnimeProvider() } returns flowOf(provider)
    }

    @Test
    fun `searchAnime delegates to Jikan when Jikan is selected`() = runTest {
        selectProvider(AnimeProvider.JIKAN)
        val page = SearchResultPage(results = emptyList(), hasNextPage = false, currentPage = 1)
        coEvery { jikanDataSource.searchAnime(any(), any(), any()) } returns Result.success(page)

        val result = dataSource.searchAnime("frieren")

        assertThat(result.getOrThrow()).isEqualTo(page)
        coVerify(exactly = 0) { malDataSource.searchAnime(any(), any(), any()) }
    }

    @Test
    fun `searchAnime delegates to MAL when MAL is selected`() = runTest {
        selectProvider(AnimeProvider.MAL)
        val page = SearchResultPage(results = emptyList(), hasNextPage = false, currentPage = 1)
        coEvery { malDataSource.searchAnime(any(), any(), any()) } returns Result.success(page)

        val result = dataSource.searchAnime("frieren")

        assertThat(result.getOrThrow()).isEqualTo(page)
        coVerify(exactly = 0) { jikanDataSource.searchAnime(any(), any(), any()) }
    }

    @Test
    fun `fetchAnimeFullById follows the selected provider per call`() = runTest {
        every { userPreferencesRepository.observeAnimeProvider() } returns
            flowOf(AnimeProvider.JIKAN) andThen flowOf(AnimeProvider.MAL)
        coEvery { jikanDataSource.fetchAnimeFullById(1) } returns Result.failure(IllegalStateException())
        coEvery { malDataSource.fetchAnimeFullById(1) } returns Result.failure(IllegalStateException())

        dataSource.fetchAnimeFullById(1)
        dataSource.fetchAnimeFullById(1)

        coVerify(exactly = 1) { jikanDataSource.fetchAnimeFullById(1) }
        coVerify(exactly = 1) { malDataSource.fetchAnimeFullById(1) }
    }

    @Test
    fun `fetchAnimeEpisodes delegates to the selected provider`() = runTest {
        selectProvider(AnimeProvider.MAL)
        val page = EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 2)
        coEvery { malDataSource.fetchAnimeEpisodes(malId = 1, page = 1) } returns Result.success(page)

        val result = dataSource.fetchAnimeEpisodes(malId = 1, page = 1)

        assertThat(result.getOrThrow()).isEqualTo(page)
    }

    @Test
    fun `fetchEpisodesAiredBetween delegates all arguments`() = runTest {
        selectProvider(AnimeProvider.JIKAN)
        val after = LocalDate.parse("2024-01-01")
        val upTo = LocalDate.parse("2024-02-01")
        coEvery {
            jikanDataSource.fetchEpisodesAiredBetween(
                malId = 1,
                after = after,
                upTo = upTo,
                startingFromEpisode = 12
            )
        } returns Result.success(emptyList())

        val result = dataSource.fetchEpisodesAiredBetween(
            malId = 1,
            after = after,
            upTo = upTo,
            startingFromEpisode = 12
        )

        assertThat(result.isSuccess).isTrue()
        coVerify {
            jikanDataSource.fetchEpisodesAiredBetween(
                malId = 1,
                after = after,
                upTo = upTo,
                startingFromEpisode = 12
            )
        }
    }

    @Test
    fun `fetchWatchOrder delegates to the selected provider`() = runTest {
        selectProvider(AnimeProvider.MAL)
        coEvery { malDataSource.fetchWatchOrder(1) } returns Result.success(emptyList())

        val result = dataSource.fetchWatchOrder(1)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 0) { jikanDataSource.fetchWatchOrder(any()) }
    }

    @Test
    fun `fetchSeasonAnime delegates to the selected provider`() = runTest {
        selectProvider(AnimeProvider.JIKAN)
        val page = SeasonalAnimePage(results = emptyList(), hasNextPage = false, currentPage = 1)
        coEvery {
            jikanDataSource.fetchSeasonAnime(year = 2023, season = AnimeSeason.FALL, page = 1, filter = any())
        } returns Result.success(page)

        val result = dataSource.fetchSeasonAnime(year = 2023, season = AnimeSeason.FALL, page = 1)

        assertThat(result.getOrThrow()).isEqualTo(page)
    }
}
