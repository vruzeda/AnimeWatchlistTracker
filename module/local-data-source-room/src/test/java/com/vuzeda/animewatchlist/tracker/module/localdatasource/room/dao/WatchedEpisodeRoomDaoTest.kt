package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.RoomDatabaseTestHelper
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.AnimeDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WatchedEpisodeRoomDaoTest {

    private lateinit var database: AnimeDatabase
    private lateinit var dao: WatchedEpisodeRoomDao

    @Before
    fun setup() {
        database = RoomDatabaseTestHelper.createInMemoryDatabase()
        dao = database.watchedEpisodeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun insertSeason(seasonId: Long) {
        val animeId = database.animeDao().insert(Anime(title = "Parent $seasonId"))
        database.seasonDao().insertAll(
            listOf(Season(id = seasonId, animeId = animeId, malId = seasonId.toInt(), title = "Season $seasonId"))
        )
    }

    @Test
    fun `markWatched inserts episode and observeWatchedEpisodeNumbers emits as set`() = runTest {
        val seasonId = 1L
        insertSeason(seasonId)
        dao.markWatched(seasonId, 1)
        dao.markWatched(seasonId, 2)
        dao.markWatched(seasonId, 3)

        dao.observeWatchedEpisodeNumbers(seasonId).test {
            val result = awaitItem()
            assertThat(result).containsExactly(1, 2, 3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markWatched ignores duplicate episodes`() = runTest {
        val seasonId = 1L
        insertSeason(seasonId)
        dao.markWatched(seasonId, 1)
        dao.markWatched(seasonId, 1)

        val result = dao.getWatchedEpisodeNumbers(seasonId)
        assertThat(result).containsExactly(1)
    }

    @Test
    fun `getWatchedEpisodeNumbers returns episodes as set`() = runTest {
        val seasonId = 1L
        insertSeason(seasonId)
        dao.markWatched(seasonId, 5)
        dao.markWatched(seasonId, 10)

        val result = dao.getWatchedEpisodeNumbers(seasonId)
        assertThat(result).containsExactly(5, 10)
    }

    @Test
    fun `getWatchedEpisodeNumbers returns empty set for season with no watched episodes`() = runTest {
        val result = dao.getWatchedEpisodeNumbers(999L)
        assertThat(result).isEmpty()
    }

    @Test
    fun `markUnwatched removes specific episode`() = runTest {
        val seasonId = 1L
        insertSeason(seasonId)
        dao.markWatched(seasonId, 1)
        dao.markWatched(seasonId, 2)
        dao.markWatched(seasonId, 3)

        dao.markUnwatched(seasonId, 2)

        val result = dao.getWatchedEpisodeNumbers(seasonId)
        assertThat(result).containsExactly(1, 3)
    }

    @Test
    fun `clearWatchedEpisodes removes all episodes for season`() = runTest {
        val seasonId1 = 1L
        val seasonId2 = 2L
        insertSeason(seasonId1)
        insertSeason(seasonId2)
        dao.markWatched(seasonId1, 1)
        dao.markWatched(seasonId1, 2)
        dao.markWatched(seasonId2, 1)

        dao.clearWatchedEpisodes(seasonId1)

        val result1 = dao.getWatchedEpisodeNumbers(seasonId1)
        val result2 = dao.getWatchedEpisodeNumbers(seasonId2)
        assertThat(result1).isEmpty()
        assertThat(result2).containsExactly(1)
    }

    @Test
    fun `deleteWatchedEpisodesAbove removes episodes above episode count`() = runTest {
        val seasonId = 1L
        insertSeason(seasonId)
        dao.markWatched(seasonId, 1)
        dao.markWatched(seasonId, 2)
        dao.markWatched(seasonId, 3)
        dao.markWatched(seasonId, 4)
        dao.markWatched(seasonId, 5)

        dao.deleteWatchedEpisodesAbove(seasonId, 2)

        val result = dao.getWatchedEpisodeNumbers(seasonId)
        assertThat(result).containsExactly(1, 2)
    }

    @Test
    fun `observeWatchedCountsForAllSeasons groups by seasonId`() = runTest {
        insertSeason(1L)
        insertSeason(2L)
        insertSeason(3L)
        dao.markWatched(1L, 1)
        dao.markWatched(1L, 2)
        dao.markWatched(2L, 1)
        dao.markWatched(2L, 2)
        dao.markWatched(2L, 3)
        dao.markWatched(3L, 5)

        dao.observeWatchedCountsForAllSeasons().test {
            val result = awaitItem()
            assertThat(result).containsExactly(1L, 2, 2L, 3, 3L, 1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markWatched for multiple seasons maintains separate counts`() = runTest {
        val seasonId1 = 1L
        val seasonId2 = 2L
        insertSeason(seasonId1)
        insertSeason(seasonId2)

        dao.markWatched(seasonId1, 1)
        dao.markWatched(seasonId1, 2)
        dao.markWatched(seasonId2, 1)

        val result1 = dao.getWatchedEpisodeNumbers(seasonId1)
        val result2 = dao.getWatchedEpisodeNumbers(seasonId2)

        assertThat(result1).containsExactly(1, 2)
        assertThat(result2).containsExactly(1)
    }
}
