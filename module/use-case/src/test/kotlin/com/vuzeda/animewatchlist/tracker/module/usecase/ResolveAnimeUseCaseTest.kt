package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.ResolvedSeries
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SequelInfo
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ResolveAnimeUseCaseTest {

    private val remoteRepository: AnimeRemoteDataSource = mockk()
    private val useCase = ResolveAnimeUseCase(remoteRepository)

    private val firstSeasonDetails = AnimeFullDetails(
        malId = 100,
        title = "Anime S1",
        titleEnglish = "Anime Season 1",
        imageUrl = "s1.jpg",
        synopsis = "First season synopsis",
        genres = listOf("Action"),
        type = "TV",
        episodes = 12,
        score = 8.5,
        sequels = listOf(SequelInfo(malId = 200, title = "Anime S2")),
        prequels = emptyList()
    )

    private val secondSeasonDetails = AnimeFullDetails(
        malId = 200,
        title = "Anime S2",
        titleEnglish = "Anime Season 2",
        imageUrl = "s2.jpg",
        synopsis = "Second season synopsis",
        genres = listOf("Action", "Drama"),
        type = "TV",
        episodes = 24,
        score = 9.0,
        sequels = emptyList(),
        prequels = listOf(SequelInfo(malId = 100, title = "Anime S1"))
    )

    private val watchOrder = listOf(
        SeasonData(malId = 100, title = "Anime S1", type = "TV", episodeCount = 12, score = 8.5, isMainSeries = true),
        SeasonData(malId = 200, title = "Anime S2", type = "TV", episodeCount = 24, score = 9.0, isMainSeries = true),
    )

    private val watchOrderWithNonMainSeriesFirstSeason = listOf(
        SeasonData(malId = 100, title = "Anime S1", type = "TV", episodeCount = 12, score = 8.5, isMainSeries = false),
        SeasonData(malId = 200, title = "Anime S2", type = "TV", episodeCount = 24, score = 9.0, isMainSeries = true),
    )

    private val watchOrderWithNoMainSeriesSeasons = listOf(
        SeasonData(malId = 100, title = "Anime S1", type = "TV", episodeCount = 12, score = 8.5, isMainSeries = false),
        SeasonData(malId = 200, title = "Anime S2", type = "TV", episodeCount = 24, score = 9.0, isMainSeries = false),
    )

    @Test
    fun `uses first season details when adding non-first season`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.success(watchOrder)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(firstSeasonDetails)

        val result = useCase(200)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(
            ResolvedSeries(
                title = "Anime S1",
                titleEnglish = "Anime Season 1",
                titleJapanese = null,
                imageUrl = "s1.jpg",
                synopsis = "First season synopsis",
                genres = listOf("Action"),
                seasons = watchOrder,
            )
        )
    }

    @Test
    fun `uses second season details when adding non-first season, but first season is not main series`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.success(watchOrderWithNonMainSeriesFirstSeason)
        coEvery { remoteRepository.fetchAnimeFullById(200) } returns Result.success(secondSeasonDetails)

        val result = useCase(200)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(
            ResolvedSeries(
                title = "Anime S2",
                titleEnglish = "Anime Season 2",
                titleJapanese = null,
                imageUrl = "s2.jpg",
                synopsis = "Second season synopsis",
                genres = listOf("Action", "Drama"),
                seasons = watchOrderWithNonMainSeriesFirstSeason,
            )
        )
    }

    @Test
    fun `uses first season details when adding non-first season, but anime has no main series seasons`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.success(watchOrderWithNoMainSeriesSeasons)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(firstSeasonDetails)

        val result = useCase(200)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(
            ResolvedSeries(
                title = "Anime S1",
                titleEnglish = "Anime Season 1",
                titleJapanese = null,
                imageUrl = "s1.jpg",
                synopsis = "First season synopsis",
                genres = listOf("Action"),
                seasons = watchOrderWithNoMainSeriesSeasons,
            )
        )
    }
}
