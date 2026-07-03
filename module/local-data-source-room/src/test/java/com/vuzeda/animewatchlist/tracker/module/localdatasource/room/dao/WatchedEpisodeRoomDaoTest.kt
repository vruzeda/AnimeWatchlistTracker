package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.RoomDatabaseTestHelper
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.AnimeDatabase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class WatchedEpisodeRoomDaoTest {

    private lateinit var database: AnimeDatabase
    private lateinit var dao: WatchedEpisodeRoomDao

    @BeforeEach
    fun setup() {
        database = RoomDatabaseTestHelper.createInMemoryDatabase()
        dao = database.watchedEpisodeDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun `markWatched inserts episode and observeWatchedEpisodeNumbers emits as set`() = runTest {
        val seasonId = 1L
        dao.markWatched(seasonId, 1)
        dao.markWatched(seasonId, 2)
        dao.markWatched(seasonId, 3)

        dao.observeWatchedEpisodeNumbers(seasonId).test {
            val result = awaitItem()
            assertThat(result).containsExactly(1, 2, 3)
            awaitComplete()
        }
    }

    @Test
    fun `markWatched ignores duplicate episodes`() = runTest {
        val seasonId = 1L
        dao.markWatched(seasonId, 1)
        dao.markWatched(seasonId, 1)

        val result = dao.getWatchedEpisodeNumbers(seasonId)
        assertThat(result).containsExactly(1)
    }

    @Test
    fun `getWatchedEpisodeNumbers returns episodes as set`() = runTest {
        val seasonId = 1L
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
        dao.markWatched(1L, 1)
        dao.markWatched(1L, 2)
        dao.markWatched(2L, 1)
        dao.markWatched(2L, 2)
        dao.markWatched(2L, 3)
        dao.markWatched(3L, 5)

        dao.observeWatchedCountsForAllSeasons().test {
            val result = awaitItem()
            assertThat(result).containsExactly(1L, 2, 2L, 3, 3L, 1)
            awaitComplete()
        }
    }

    @Test
    fun `markWatched for multiple seasons maintains separate counts`() = runTest {
        val seasonId1 = 1L
        val seasonId2 = 2L

        dao.markWatched(seasonId1, 1)
        dao.markWatched(seasonId1, 2)
        dao.markWatched(seasonId2, 1)

        val result1 = dao.getWatchedEpisodeNumbers(seasonId1)
        val result2 = dao.getWatchedEpisodeNumbers(seasonId2)

        assertThat(result1).containsExactly(1, 2)
        assertThat(result2).containsExactly(1)
    }
}
