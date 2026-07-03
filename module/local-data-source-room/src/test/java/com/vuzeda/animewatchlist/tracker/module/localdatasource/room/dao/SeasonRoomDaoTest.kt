package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.RoomDatabaseTestHelper
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.AnimeDatabase
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.SeasonEntity
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class SeasonRoomDaoTest {

    private lateinit var database: AnimeDatabase
    private lateinit var dao: SeasonRoomDao

    @BeforeEach
    fun setup() {
        database = RoomDatabaseTestHelper.createInMemoryDatabase()
        dao = database.seasonDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertAll and observeAll returns inserted seasons ordered by animeId and orderIndex`() = runTest {
        val animeId = 123L
        val season1 = Season(animeId = animeId, malId = 1001, title = "Season 1", orderIndex = 0)
        val season2 = Season(animeId = animeId, malId = 1002, title = "Season 2", orderIndex = 1)

        dao.insertAll(listOf(season1, season2))

        dao.observeAll().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result[0].malId).isEqualTo(1001)
            assertThat(result[1].malId).isEqualTo(1002)
            awaitComplete()
        }
    }

    @Test
    fun `observeByAnimeId returns only seasons for that anime ordered by orderIndex`() = runTest {
        val animeId1 = 100L
        val animeId2 = 200L
        val seasons = listOf(
            Season(animeId = animeId1, malId = 1001, title = "A1", orderIndex = 1),
            Season(animeId = animeId1, malId = 1002, title = "A2", orderIndex = 0),
            Season(animeId = animeId2, malId = 2001, title = "B1", orderIndex = 0)
        )

        dao.insertAll(seasons)

        dao.observeByAnimeId(animeId1).test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result[0].orderIndex).isEqualTo(0)
            assertThat(result[1].orderIndex).isEqualTo(1)
            awaitComplete()
        }
    }

    @Test
    fun `findByMalId returns season with matching malId`() = runTest {
        val malId = 5555
        val season = Season(animeId = 1L, malId = malId, title = "Test Season")

        dao.insertAll(listOf(season))

        val found = dao.findByMalId(malId)
        assertThat(found).isNotNull()
        assertThat(found?.malId).isEqualTo(malId)
    }

    @Test
    fun `findByMalId returns null for non-existent malId`() = runTest {
        val found = dao.findByMalId(9999)
        assertThat(found).isNull()
    }

    @Test
    fun `getByAnimeId returns seasons in orderIndex order`() = runTest {
        val animeId = 100L
        val seasons = listOf(
            Season(animeId = animeId, malId = 1001, title = "S1", orderIndex = 2),
            Season(animeId = animeId, malId = 1002, title = "S2", orderIndex = 0),
            Season(animeId = animeId, malId = 1003, title = "S3", orderIndex = 1)
        )

        dao.insertAll(seasons)

        val result = dao.getByAnimeId(animeId)
        assertThat(result).hasSize(3)
        assertThat(result[0].orderIndex).isEqualTo(0)
        assertThat(result[1].orderIndex).isEqualTo(1)
        assertThat(result[2].orderIndex).isEqualTo(2)
    }

    @Test
    fun `update modifies season fields`() = runTest {
        val season = Season(animeId = 1L, malId = 1001, title = "Original", id = 1)
        dao.insertAll(listOf(season))

        val updated = season.copy(title = "Updated", score = 8.5)
        dao.update(updated)

        val retrieved = dao.findByMalId(1001)
        assertThat(retrieved?.title).isEqualTo("Updated")
        assertThat(retrieved?.score).isEqualTo(8.5)
    }

    @Test
    fun `updateNotificationData updates lastCheckedAiredEpisodeCount`() = runTest {
        val season = Season(animeId = 1L, malId = 1001, title = "Test", id = 1)
        dao.insertAll(listOf(season))

        dao.updateNotificationData(1, 5)

        val retrieved = dao.findByMalId(1001)
        assertThat(retrieved?.lastCheckedAiredEpisodeCount).isEqualTo(5)
    }

    @Test
    fun `updateEpisodeNotificationsEnabled toggles the flag`() = runTest {
        val season = Season(animeId = 1L, malId = 1001, title = "Test", id = 1, isEpisodeNotificationsEnabled = false)
        dao.insertAll(listOf(season))

        dao.updateEpisodeNotificationsEnabled(1, true)

        val retrieved = dao.findByMalId(1001)
        assertThat(retrieved?.isEpisodeNotificationsEnabled).isTrue()
    }

    @Test
    fun `getSeasonsWithEpisodeNotifications returns only enabled seasons`() = runTest {
        val enabled = Season(animeId = 1L, malId = 1001, title = "Enabled", isEpisodeNotificationsEnabled = true)
        val disabled = Season(animeId = 1L, malId = 1002, title = "Disabled", isEpisodeNotificationsEnabled = false)

        dao.insertAll(listOf(enabled, disabled))

        val result = dao.getSeasonsWithEpisodeNotifications()
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Enabled")
    }

    @Test
    fun `observeAllMalIds returns malIds for watchlist seasons only`() = runTest {
        val inWatchlist = Season(animeId = 1L, malId = 1001, title = "In", isInWatchlist = true)
        val notInWatchlist = Season(animeId = 1L, malId = 1002, title = "Out", isInWatchlist = false)

        dao.insertAll(listOf(inWatchlist, notInWatchlist))

        dao.observeAllMalIds().test {
            val result = awaitItem()
            assertThat(result).containsExactly(1001)
            awaitComplete()
        }
    }

    @Test
    fun `deleteById removes only the specified season`() = runTest {
        val season1 = Season(animeId = 1L, malId = 1001, title = "S1", id = 1)
        val season2 = Season(animeId = 1L, malId = 1002, title = "S2", id = 2)

        dao.insertAll(listOf(season1, season2))
        dao.deleteById(1)

        val remaining = dao.getByAnimeId(1)
        assertThat(remaining).hasSize(1)
        assertThat(remaining[0].malId).isEqualTo(1002)
    }
}
