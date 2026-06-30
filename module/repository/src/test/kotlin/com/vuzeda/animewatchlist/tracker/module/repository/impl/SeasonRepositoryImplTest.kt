package com.vuzeda.animewatchlist.tracker.module.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.localdatasource.SeasonLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.WatchedEpisodeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.TransactionRunner
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SeasonRepositoryImplTest {

    private val seasonLocalDataSource: SeasonLocalDataSource = mockk()
    private val watchedEpisodeLocalDataSource: WatchedEpisodeLocalDataSource = mockk()
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val repository = SeasonRepositoryImpl(seasonLocalDataSource, watchedEpisodeLocalDataSource, transactionRunner)

    private val sampleSeason = Season(
        id = 1L,
        animeId = 1L,
        malId = 16498,
        title = "Attack on Titan",
        type = "TV",
        episodeCount = 25,
        score = 8.5,
        orderIndex = 0
    )

    @Test
    fun `observeAllSeasons emits all seasons from data source`() = runTest {
        every { seasonLocalDataSource.observeAll() } returns flowOf(listOf(sampleSeason))
        every { watchedEpisodeLocalDataSource.observeWatchedCountsForAllSeasons() } returns flowOf(emptyMap())

        repository.observeAllSeasons().test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].malId).isEqualTo(16498)
            awaitComplete()
        }
    }

    @Test
    fun `observeAllSeasons populates watchedEpisodeCount from watched data`() = runTest {
        every { seasonLocalDataSource.observeAll() } returns flowOf(listOf(sampleSeason))
        every { watchedEpisodeLocalDataSource.observeWatchedCountsForAllSeasons() } returns flowOf(mapOf(1L to 5))

        repository.observeAllSeasons().test {
            val result = awaitItem()

            assertThat(result[0].watchedEpisodeCount).isEqualTo(5)
            awaitComplete()
        }
    }

    @Test
    fun `observeSeasonsForAnime emits mapped season domain models`() = runTest {
        every { seasonLocalDataSource.observeByAnimeId(1L) } returns flowOf(listOf(sampleSeason))
        every { watchedEpisodeLocalDataSource.observeWatchedCountsForAllSeasons() } returns flowOf(emptyMap())

        repository.observeSeasonsForAnime(1L).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].malId).isEqualTo(16498)
            assertThat(result[0].title).isEqualTo("Attack on Titan")
            awaitComplete()
        }
    }

    @Test
    fun `observeSeasonsForAnime populates watchedEpisodeCount from watched data`() = runTest {
        every { seasonLocalDataSource.observeByAnimeId(1L) } returns flowOf(listOf(sampleSeason))
        every { watchedEpisodeLocalDataSource.observeWatchedCountsForAllSeasons() } returns flowOf(mapOf(1L to 3))

        repository.observeSeasonsForAnime(1L).test {
            val result = awaitItem()

            assertThat(result[0].watchedEpisodeCount).isEqualTo(3)
            awaitComplete()
        }
    }

    @Test
    fun `observeSeasonById emits mapped domain model`() = runTest {
        every { seasonLocalDataSource.observeById(1L) } returns flowOf(sampleSeason)
        every { watchedEpisodeLocalDataSource.observeWatchedEpisodeNumbers(1L) } returns flowOf(emptySet())

        repository.observeSeasonById(1L).test {
            val result = awaitItem()

            assertThat(result).isNotNull()
            assertThat(result?.malId).isEqualTo(16498)
            assertThat(result?.title).isEqualTo("Attack on Titan")
            awaitComplete()
        }
    }

    @Test
    fun `observeSeasonById populates watchedEpisodeCount from watched data`() = runTest {
        every { seasonLocalDataSource.observeById(1L) } returns flowOf(sampleSeason)
        every { watchedEpisodeLocalDataSource.observeWatchedEpisodeNumbers(1L) } returns flowOf(setOf(1, 2, 4))

        repository.observeSeasonById(1L).test {
            val result = awaitItem()

            assertThat(result?.watchedEpisodeCount).isEqualTo(3)
            awaitComplete()
        }
    }

    @Test
    fun `observeSeasonById emits null when not found`() = runTest {
        every { seasonLocalDataSource.observeById(999L) } returns flowOf(null)

        repository.observeSeasonById(999L).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `findAnimeIdBySeasonMalId returns animeId when found`() = runTest {
        coEvery { seasonLocalDataSource.findByMalId(16498) } returns sampleSeason

        val result = repository.findAnimeIdBySeasonMalId(16498)

        assertThat(result).isEqualTo(1L)
    }

    @Test
    fun `findAnimeIdBySeasonMalId returns null when not found`() = runTest {
        coEvery { seasonLocalDataSource.findByMalId(99999) } returns null

        val result = repository.findAnimeIdBySeasonMalId(99999)

        assertThat(result).isNull()
    }

    @Test
    fun `findSeasonIdByMalId returns season id when found`() = runTest {
        coEvery { seasonLocalDataSource.findByMalId(16498) } returns sampleSeason

        val result = repository.findSeasonIdByMalId(16498)

        assertThat(result).isEqualTo(1L)
    }

    @Test
    fun `findSeasonIdByMalId returns null when not found`() = runTest {
        coEvery { seasonLocalDataSource.findByMalId(99999) } returns null

        val result = repository.findSeasonIdByMalId(99999)

        assertThat(result).isNull()
    }

    @Test
    fun `getSeasonsForAnime returns mapped season domain models`() = runTest {
        coEvery { seasonLocalDataSource.getByAnimeId(1L) } returns listOf(sampleSeason)

        val result = repository.getSeasonsForAnime(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].malId).isEqualTo(16498)
    }

    @Test
    fun `addSeasonsToAnime inserts seasons with correct animeId`() = runTest {
        coEvery { seasonLocalDataSource.insertAll(any()) } returns Unit

        val seasons = listOf(
            Season(malId = 200, title = "Season 2", orderIndex = 1)
        )

        repository.addSeasonsToAnime(animeId = 5L, seasons = seasons)

        val seasonSlot = slot<List<Season>>()
        coVerify { seasonLocalDataSource.insertAll(capture(seasonSlot)) }
        assertThat(seasonSlot.captured[0].animeId).isEqualTo(5L)
        assertThat(seasonSlot.captured[0].malId).isEqualTo(200)
    }

    @Test
    fun `updateSeason delegates to data source`() = runTest {
        coEvery { seasonLocalDataSource.update(any()) } returns Unit

        repository.updateSeason(Season(id = 1L, animeId = 1L, malId = 100, title = "S1"))

        coVerify { seasonLocalDataSource.update(any()) }
    }

    @Test
    fun `updateSeasonNotificationData delegates to data source`() = runTest {
        coEvery { seasonLocalDataSource.updateNotificationData(any(), any()) } returns Unit

        repository.updateSeasonNotificationData(seasonId = 1L, lastCheckedAiredEpisodeCount = 25)

        coVerify { seasonLocalDataSource.updateNotificationData(seasonId = 1L, count = 25) }
    }

    @Test
    fun `updateSeasonNotificationData passes null count to data source`() = runTest {
        coEvery { seasonLocalDataSource.updateNotificationData(any(), any()) } returns Unit

        repository.updateSeasonNotificationData(seasonId = 1L, lastCheckedAiredEpisodeCount = null)

        coVerify { seasonLocalDataSource.updateNotificationData(seasonId = 1L, count = null) }
    }

    @Test
    fun `toggleSeasonEpisodeNotifications enables notifications`() = runTest {
        coEvery { seasonLocalDataSource.updateEpisodeNotificationsEnabled(any(), any()) } returns Unit

        repository.toggleSeasonEpisodeNotifications(seasonId = 1L, enabled = true)

        coVerify { seasonLocalDataSource.updateEpisodeNotificationsEnabled(seasonId = 1L, enabled = true) }
    }

    @Test
    fun `toggleSeasonEpisodeNotifications disables notifications`() = runTest {
        coEvery { seasonLocalDataSource.updateEpisodeNotificationsEnabled(any(), any()) } returns Unit

        repository.toggleSeasonEpisodeNotifications(seasonId = 1L, enabled = false)

        coVerify { seasonLocalDataSource.updateEpisodeNotificationsEnabled(seasonId = 1L, enabled = false) }
    }

    @Test
    fun `getSeasonsWithEpisodeNotifications returns seasons from data source`() = runTest {
        val notificationSeason = sampleSeason.copy(isEpisodeNotificationsEnabled = true)
        coEvery { seasonLocalDataSource.getSeasonsWithEpisodeNotifications() } returns listOf(notificationSeason)

        val result = repository.getSeasonsWithEpisodeNotifications()

        assertThat(result).hasSize(1)
        assertThat(result[0].isEpisodeNotificationsEnabled).isTrue()
    }

    @Test
    fun `observeAllSeasonMalIds emits set of malIds`() = runTest {
        every { seasonLocalDataSource.observeAllMalIds() } returns flowOf(listOf(100, 200, 300))

        repository.observeAllSeasonMalIds().test {
            val result = awaitItem()
            assertThat(result).containsExactly(100, 200, 300)
            awaitComplete()
        }
    }

    @Test
    fun `updateLatestKnownEpisodeAirDate delegates to data source`() = runTest {
        val date = LocalDate.of(2026, 3, 15)
        coEvery { seasonLocalDataSource.updateLatestKnownEpisodeAirDate(1L, date) } returns Unit

        repository.updateLatestKnownEpisodeAirDate(1L, date)

        coVerify { seasonLocalDataSource.updateLatestKnownEpisodeAirDate(1L, date) }
    }

    @Test
    fun `updateLastEpisodeCheckPerformedDate delegates to data source`() = runTest {
        val date = LocalDate.of(2026, 3, 15)
        coEvery { seasonLocalDataSource.updateLastEpisodeCheckPerformedDate(1L, date) } returns Unit

        repository.updateLastEpisodeCheckPerformedDate(1L, date)

        coVerify { seasonLocalDataSource.updateLastEpisodeCheckPerformedDate(1L, date) }
    }

    @Test
    fun `observeWatchedEpisodesForSeason emits set from data source`() = runTest {
        every { watchedEpisodeLocalDataSource.observeWatchedEpisodeNumbers(1L) } returns flowOf(setOf(1, 3))

        repository.observeWatchedEpisodesForSeason(1L).test {
            val result = awaitItem()

            assertThat(result).containsExactly(1, 3)
            awaitComplete()
        }
    }

    @Test
    fun `setEpisodeWatched marks episode as watched`() = runTest {
        coEvery { watchedEpisodeLocalDataSource.markWatched(1L, 5) } returns Unit

        repository.setEpisodeWatched(seasonId = 1L, episodeNumber = 5, isWatched = true)

        coVerify { watchedEpisodeLocalDataSource.markWatched(1L, 5) }
        coVerify(exactly = 0) { watchedEpisodeLocalDataSource.markUnwatched(any(), any()) }
    }

    @Test
    fun `setEpisodeWatched marks episode as unwatched`() = runTest {
        coEvery { watchedEpisodeLocalDataSource.markUnwatched(1L, 5) } returns Unit

        repository.setEpisodeWatched(seasonId = 1L, episodeNumber = 5, isWatched = false)

        coVerify { watchedEpisodeLocalDataSource.markUnwatched(1L, 5) }
        coVerify(exactly = 0) { watchedEpisodeLocalDataSource.markWatched(any(), any()) }
    }

    @Test
    fun `removeSeasonFromWatchlist resets watchlist fields and clears watched episodes`() = runTest {
        val watchlistSeason = sampleSeason.copy(
            isInWatchlist = true,
            isEpisodeNotificationsEnabled = true,
            addedAt = 1000L
        )
        coEvery { seasonLocalDataSource.update(any()) } returns Unit
        coJustRun { watchedEpisodeLocalDataSource.clearWatchedEpisodes(1L) }

        repository.removeSeasonFromWatchlist(watchlistSeason)

        val seasonSlot = slot<Season>()
        coVerify { seasonLocalDataSource.update(capture(seasonSlot)) }
        assertThat(seasonSlot.captured.isInWatchlist).isFalse()
        assertThat(seasonSlot.captured.status).isEqualTo(WatchStatus.PLAN_TO_WATCH)
        assertThat(seasonSlot.captured.isEpisodeNotificationsEnabled).isFalse()
        assertThat(seasonSlot.captured.lastCheckedAiredEpisodeCount).isNull()
        assertThat(seasonSlot.captured.latestKnownEpisodeAirDate).isNull()
        assertThat(seasonSlot.captured.lastEpisodeCheckPerformedDate).isNull()
        assertThat(seasonSlot.captured.addedAt).isEqualTo(0L)
        coVerify { watchedEpisodeLocalDataSource.clearWatchedEpisodes(1L) }
    }

    @Test
    fun `upsertSeasonsFromWatchOrder updates metadata for seasons already in the database`() = runTest {
        val season = SeasonData(malId = 16498, title = "New Title", type = "TV", episodeCount = 26)
        coEvery { seasonLocalDataSource.getByAnimeId(1L) } returns listOf(sampleSeason)
        coEvery { seasonLocalDataSource.updateSeasonMetadata(any()) } returns Unit

        repository.upsertSeasonsFromWatchOrder(animeId = 1L, seasons = listOf(season))

        val captured = slot<SeasonData>()
        coVerify { seasonLocalDataSource.updateSeasonMetadata(capture(captured)) }
        assertThat(captured.captured.malId).isEqualTo(16498)
        assertThat(captured.captured.title).isEqualTo("New Title")
        assertThat(captured.captured.episodeCount).isEqualTo(26)
        coVerify(exactly = 0) { seasonLocalDataSource.insertAll(any()) }
    }

    @Test
    fun `upsertSeasonsFromWatchOrder preserves existing episodeCount when incoming data has null`() = runTest {
        val incomingWithoutEpisodeCount = SeasonData(malId = 16498, title = "New Title", type = "TV")
        coEvery { seasonLocalDataSource.getByAnimeId(1L) } returns listOf(sampleSeason)
        coEvery { seasonLocalDataSource.updateSeasonMetadata(any()) } returns Unit

        repository.upsertSeasonsFromWatchOrder(animeId = 1L, seasons = listOf(incomingWithoutEpisodeCount))

        val slot = slot<SeasonData>()
        coVerify { seasonLocalDataSource.updateSeasonMetadata(capture(slot)) }
        assertThat(slot.captured.episodeCount).isEqualTo(25)
    }

    @Test
    fun `upsertSeasonsFromWatchOrder preserves existing nullable metadata fields when incoming data has nulls`() = runTest {
        val existingSeason = sampleSeason.copy(
            titleEnglish = "Attack on Titan",
            titleJapanese = "進撃の巨人",
            imageUrl = "https://example.com/aot.jpg",
            score = 8.5,
            airingStatus = "Finished Airing"
        )
        val incomingWithNulls = SeasonData(malId = 16498, title = "Shingeki no Kyojin", type = "TV")
        coEvery { seasonLocalDataSource.getByAnimeId(1L) } returns listOf(existingSeason)
        coEvery { seasonLocalDataSource.updateSeasonMetadata(any()) } returns Unit

        repository.upsertSeasonsFromWatchOrder(animeId = 1L, seasons = listOf(incomingWithNulls))

        val slot = slot<SeasonData>()
        coVerify { seasonLocalDataSource.updateSeasonMetadata(capture(slot)) }
        assertThat(slot.captured.titleEnglish).isEqualTo("Attack on Titan")
        assertThat(slot.captured.titleJapanese).isEqualTo("進撃の巨人")
        assertThat(slot.captured.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(slot.captured.score).isEqualTo(8.5)
        assertThat(slot.captured.airingStatus).isEqualTo("Finished Airing")
    }

    @Test
    fun `upsertSeasonsFromWatchOrder uses incoming non-null values over existing ones`() = runTest {
        val existingSeason = sampleSeason.copy(episodeCount = 25, score = 8.5, airingStatus = "Currently Airing")
        val incomingWithValues = SeasonData(
            malId = 16498,
            title = "New Title",
            type = "TV",
            episodeCount = 26,
            score = 9.0,
            airingStatus = "Finished Airing"
        )
        coEvery { seasonLocalDataSource.getByAnimeId(1L) } returns listOf(existingSeason)
        coEvery { seasonLocalDataSource.updateSeasonMetadata(any()) } returns Unit

        repository.upsertSeasonsFromWatchOrder(animeId = 1L, seasons = listOf(incomingWithValues))

        val slot = slot<SeasonData>()
        coVerify { seasonLocalDataSource.updateSeasonMetadata(capture(slot)) }
        assertThat(slot.captured.episodeCount).isEqualTo(26)
        assertThat(slot.captured.score).isEqualTo(9.0)
        assertThat(slot.captured.airingStatus).isEqualTo("Finished Airing")
    }

    @Test
    fun `upsertSeasonsFromWatchOrder inserts unknown seasons with isInWatchlist false`() = runTest {
        val season = SeasonData(malId = 999, title = "New Season", type = "TV")
        coEvery { seasonLocalDataSource.getByAnimeId(1L) } returns listOf(sampleSeason)
        coEvery { seasonLocalDataSource.insertAll(any()) } returns Unit

        repository.upsertSeasonsFromWatchOrder(animeId = 1L, seasons = listOf(season))

        val slot = slot<List<Season>>()
        coVerify { seasonLocalDataSource.insertAll(capture(slot)) }
        assertThat(slot.captured).hasSize(1)
        assertThat(slot.captured[0].malId).isEqualTo(999)
        assertThat(slot.captured[0].animeId).isEqualTo(1L)
        assertThat(slot.captured[0].isInWatchlist).isFalse()
        coVerify(exactly = 0) { seasonLocalDataSource.updateSeasonMetadata(any()) }
    }

    @Test
    fun `upsertSeasonsFromWatchOrder maps start date to airing season name`() = runTest {
        coEvery { seasonLocalDataSource.getByAnimeId(1L) } returns emptyList()
        coEvery { seasonLocalDataSource.insertAll(any()) } returns Unit

        val seasons = listOf(
            SeasonData(malId = 1, title = "W", type = "TV", startDate = LocalDate.of(2026, 1, 1)),
            SeasonData(malId = 2, title = "Sp", type = "TV", startDate = LocalDate.of(2026, 4, 1)),
            SeasonData(malId = 3, title = "Su", type = "TV", startDate = LocalDate.of(2026, 7, 1)),
            SeasonData(malId = 4, title = "F", type = "TV", startDate = LocalDate.of(2026, 10, 1)),
        )
        repository.upsertSeasonsFromWatchOrder(animeId = 1L, seasons)

        val captured = slot<List<Season>>()
        coVerify { seasonLocalDataSource.insertAll(capture(captured)) }
        assertThat(captured.captured.map { it.airingSeasonName })
            .containsExactly("winter", "spring", "summer", "fall").inOrder()
    }

    @Test
    fun `getWatchedEpisodeNumbers delegates to watched episode local data source`() = runTest {
        coEvery { watchedEpisodeLocalDataSource.getWatchedEpisodeNumbers(10L) } returns setOf(1, 2, 3)

        val result = repository.getWatchedEpisodeNumbers(10L)

        assertThat(result).containsExactly(1, 2, 3)
    }

    @Test
    fun `removeSeasonFromWatchlist preserves api data`() = runTest {
        val watchlistSeason = sampleSeason.copy(isInWatchlist = true)
        coEvery { seasonLocalDataSource.update(any()) } returns Unit
        coJustRun { watchedEpisodeLocalDataSource.clearWatchedEpisodes(1L) }

        repository.removeSeasonFromWatchlist(watchlistSeason)

        val seasonSlot = slot<Season>()
        coVerify { seasonLocalDataSource.update(capture(seasonSlot)) }
        assertThat(seasonSlot.captured.malId).isEqualTo(16498)
        assertThat(seasonSlot.captured.title).isEqualTo("Attack on Titan")
        assertThat(seasonSlot.captured.type).isEqualTo("TV")
        assertThat(seasonSlot.captured.episodeCount).isEqualTo(25)
    }
}
