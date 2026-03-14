package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.SequelInfo
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class AddAnimeFromDetailsUseCaseTest {

    private val animeRepository: AnimeRepository = mockk()
    private val remoteRepository: AnimeRemoteDataSource = mockk()
    private val clock: Clock = mockk {
        coEvery { now() } returns Instant.fromEpochMilliseconds(1770294088886)
    }
    private val useCase = AddAnimeFromDetailsUseCase(animeRepository, remoteRepository, clock)

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
        SeasonData(malId = 100, title = "Anime S1", type = "TV", episodeCount = 12, score = 8.5),
        SeasonData(malId = 200, title = "Anime S2", type = "TV", episodeCount = 24, score = 9.0)
    )

    private val watchOrderWithNonMainSeriesFirstSeason = listOf(
        SeasonData(malId = 100, title = "Anime S1", type = "TV", episodeCount = 12, score = 8.5, isMainSeries = false),
        SeasonData(malId = 200, title = "Anime S2", type = "TV", episodeCount = 24, score = 9.0, isMainSeries = true),
    )

    private val watchOrderWithNoMainSeriesSeasons = listOf(
        SeasonData(malId = 100, title = "Anime S1", type = "TV", episodeCount = 12, score = 8.5, isMainSeries = false),
        SeasonData(malId = 200, title = "Anime S2", type = "TV", episodeCount = 24, score = 9.0, isMainSeries = false),
    )

    @BeforeEach
    fun setup() {
        coEvery { animeRepository.addAnime(any(), any()) } returns 1L
    }

    @Test
    fun `uses first season details when adding non-first season`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.success(watchOrder)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(firstSeasonDetails)

        useCase(secondSeasonDetails, WatchStatus.WATCHING)

        val animeSlot = slot<Anime>()
        coVerify { animeRepository.addAnime(capture(animeSlot), any()) }

        val anime = animeSlot.captured
        assertThat(anime).isEqualTo(
            Anime(
                id = 0,
                title = "Anime S1",
                titleEnglish = "Anime Season 1",
                titleJapanese = null,
                imageUrl = "s1.jpg",
                synopsis = "First season synopsis",
                genres = listOf("Action"),
                status = WatchStatus.WATCHING,
                userRating = null,
                notificationType = NotificationType.NONE,
                addedAt = 1770294088886,
            )
        )
    }

    @Test
    fun `uses second season details when adding non-first season, but first season is not main series`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.success(watchOrderWithNonMainSeriesFirstSeason)
        coEvery { remoteRepository.fetchAnimeFullById(200) } returns Result.success(secondSeasonDetails)

        useCase(secondSeasonDetails, WatchStatus.WATCHING)

        val animeSlot = slot<Anime>()
        coVerify { animeRepository.addAnime(capture(animeSlot), any()) }

        val anime = animeSlot.captured
        assertThat(anime).isEqualTo(
            Anime(
                id = 0,
                title = "Anime S2",
                titleEnglish = "Anime Season 2",
                titleJapanese = null,
                imageUrl = "s2.jpg",
                synopsis = "Second season synopsis",
                genres = listOf("Action", "Drama"),
                status = WatchStatus.WATCHING,
                userRating = null,
                notificationType = NotificationType.NONE,
                addedAt = 1770294088886,
            )
        )
    }

    @Test
    fun `uses first season details when adding non-first season, but anime has no main series seasons`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.success(watchOrderWithNoMainSeriesSeasons)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(firstSeasonDetails)

        useCase(secondSeasonDetails, WatchStatus.WATCHING)

        val animeSlot = slot<Anime>()
        coVerify { animeRepository.addAnime(capture(animeSlot), any()) }

        val anime = animeSlot.captured
        assertThat(anime).isEqualTo(
            Anime(
                id = 0,
                title = "Anime S1",
                titleEnglish = "Anime Season 1",
                titleJapanese = null,
                imageUrl = "s1.jpg",
                synopsis = "First season synopsis",
                genres = listOf("Action"),
                status = WatchStatus.WATCHING,
                userRating = null,
                notificationType = NotificationType.NONE,
                addedAt = 1770294088886,
            )
        )
    }

    @Test
    fun `sets correct orderIndex for non-first season`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.success(watchOrder)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.success(firstSeasonDetails)

        useCase(secondSeasonDetails, WatchStatus.WATCHING)

        val seasonsSlot = slot<List<com.vuzeda.animewatchlist.tracker.module.domain.Season>>()
        coVerify { animeRepository.addAnime(any(), capture(seasonsSlot)) }

        val season = seasonsSlot.captured[0]
        assertThat(season.malId).isEqualTo(200)
        assertThat(season.orderIndex).isEqualTo(1)
    }

    @Test
    fun `uses provided details when season has no prequels`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(100) } returns Result.success(watchOrder)

        useCase(firstSeasonDetails, WatchStatus.PLAN_TO_WATCH)

        val animeSlot = slot<Anime>()
        coVerify { animeRepository.addAnime(capture(animeSlot), any()) }

        val anime = animeSlot.captured
        assertThat(anime.title).isEqualTo("Anime S1")
    }

    @Test
    fun `sets orderIndex to 0 for first season`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(100) } returns Result.success(watchOrder)

        useCase(firstSeasonDetails, WatchStatus.PLAN_TO_WATCH)

        val seasonsSlot = slot<List<com.vuzeda.animewatchlist.tracker.module.domain.Season>>()
        coVerify { animeRepository.addAnime(any(), capture(seasonsSlot)) }

        assertThat(seasonsSlot.captured[0].orderIndex).isEqualTo(0)
    }

    @Test
    fun `falls back to provided details when watch order fetch fails`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.failure(Exception("Network error"))

        useCase(secondSeasonDetails, WatchStatus.WATCHING)

        val animeSlot = slot<Anime>()
        coVerify { animeRepository.addAnime(capture(animeSlot), any()) }

        val anime = animeSlot.captured
        assertThat(anime.title).isEqualTo("Anime S2")
    }

    @Test
    fun `falls back to provided details when first season fetch fails`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.success(watchOrder)
        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.failure(Exception("Not found"))

        useCase(secondSeasonDetails, WatchStatus.WATCHING)

        val animeSlot = slot<Anime>()
        coVerify { animeRepository.addAnime(capture(animeSlot), any()) }

        val anime = animeSlot.captured
        assertThat(anime.title).isEqualTo("Anime S2")
    }

    @Test
    fun `returns animeId from repository`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(100) } returns Result.success(watchOrder)
        coEvery { animeRepository.addAnime(any(), any()) } returns 42L

        val result = useCase(firstSeasonDetails, WatchStatus.WATCHING)

        assertThat(result).isEqualTo(42L)
    }
}
