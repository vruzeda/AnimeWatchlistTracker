package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.RoomDatabaseTestHelper
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.AnimeDatabase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class AnimeRoomDaoTest {

    private lateinit var database: AnimeDatabase
    private lateinit var dao: AnimeRoomDao

    @BeforeEach
    fun setup() {
        database = RoomDatabaseTestHelper.createInMemoryDatabase()
        dao = database.animeDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and observeAll returns inserted anime ordered by title`() = runTest {
        val anime1 = Anime(malId = 1001, title = "Anime Z")
        val anime2 = Anime(malId = 1002, title = "Anime A")

        dao.insert(anime1)
        dao.insert(anime2)

        dao.observeAll().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result[0].title).isEqualTo("Anime A")
            assertThat(result[1].title).isEqualTo("Anime Z")
            awaitComplete()
        }
    }

    @Test
    fun `getById returns anime with matching id`() = runTest {
        val anime = Anime(malId = 1001, title = "Test Anime")
        val id = dao.insert(anime)

        val retrieved = dao.getById(id)
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.title).isEqualTo("Test Anime")
    }

    @Test
    fun `getById returns null for non-existent id`() = runTest {
        val retrieved = dao.getById(9999L)
        assertThat(retrieved).isNull()
    }

    @Test
    fun `update modifies anime fields`() = runTest {
        val anime = Anime(malId = 1001, title = "Original", id = 1)
        dao.insert(anime)

        val updated = anime.copy(title = "Updated")
        dao.update(updated)

        val retrieved = dao.getById(1)
        assertThat(retrieved?.title).isEqualTo("Updated")
    }

    @Test
    fun `deleteById removes only specified anime`() = runTest {
        val anime1 = Anime(malId = 1001, title = "A1", id = 1)
        val anime2 = Anime(malId = 1002, title = "A2", id = 2)
        dao.insert(anime1)
        dao.insert(anime2)

        dao.deleteById(1)

        val all = dao.observeAll().test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].malId).isEqualTo(1002)
            awaitComplete()
        }
    }

    @Test
    fun `deleteAll removes all anime`() = runTest {
        dao.insert(Anime(malId = 1001, title = "A1"))
        dao.insert(Anime(malId = 1002, title = "A2"))

        dao.deleteAll()

        dao.observeAll().test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `getNotificationEnabledAnime returns only anime with notification enabled`() = runTest {
        val enabled = Anime(malId = 1001, title = "Enabled", notificationType = NotificationType.NEW_EPISODE)
        val disabled1 = Anime(malId = 1002, title = "Disabled", notificationType = NotificationType.NONE)
        val disabled2 = Anime(malId = 1003, title = "Disabled2", notificationType = NotificationType.NONE)

        dao.insert(enabled)
        dao.insert(disabled1)
        dao.insert(disabled2)

        val result = dao.getNotificationEnabledAnime()
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Enabled")
    }

    @Test
    fun `updateNotificationType changes notification setting`() = runTest {
        val anime = Anime(malId = 1001, title = "Test", id = 1, notificationType = NotificationType.NONE)
        dao.insert(anime)

        dao.updateNotificationType(1, NotificationType.NEW_EPISODE)

        val retrieved = dao.getById(1)
        assertThat(retrieved?.notificationType).isEqualTo(NotificationType.NEW_EPISODE)
    }

    @Test
    fun `updateLatestKnownSeasonStartDate updates date field`() = runTest {
        val anime = Anime(malId = 1001, title = "Test", id = 1)
        dao.insert(anime)

        val date = LocalDate.of(2024, 3, 15)
        dao.updateLatestKnownSeasonStartDate(1, date)

        val retrieved = dao.getById(1)
        assertThat(retrieved?.latestKnownSeasonStartDate).isEqualTo(date)
    }

    @Test
    fun `updateLastSeasonCheckPerformedDate updates date field`() = runTest {
        val anime = Anime(malId = 1001, title = "Test", id = 1)
        dao.insert(anime)

        val date = LocalDate.of(2024, 3, 20)
        dao.updateLastSeasonCheckPerformedDate(1, date)

        val retrieved = dao.getById(1)
        assertThat(retrieved?.lastSeasonCheckPerformedDate).isEqualTo(date)
    }

    @Test
    fun `recordAnimeUpdateAttempt records success`() = runTest {
        val anime = Anime(malId = 1001, title = "Test", id = 1)
        dao.insert(anime)

        val now = System.currentTimeMillis()
        dao.recordAnimeUpdateAttempt(now, AnimeUpdateResult.Success)

        dao.observeLastAnimeUpdateRun().test {
            val runTime = awaitItem()
            assertThat(runTime).isEqualTo(now)
            awaitComplete()
        }
    }

    @Test
    fun `recordAnimeUpdateAttempt records retry with attempt count`() = runTest {
        val anime = Anime(malId = 1001, title = "Test", id = 1)
        dao.insert(anime)

        val now = System.currentTimeMillis()
        dao.recordAnimeUpdateAttempt(now, AnimeUpdateResult.WillRetry("Network error", 1))

        dao.observeLastAnimeUpdateAttemptRetryCount().test {
            val count = awaitItem()
            assertThat(count).isEqualTo(1)
            awaitComplete()
        }
    }

    @Test
    fun `recordAnimeUpdateAttempt records failure reason`() = runTest {
        val anime = Anime(malId = 1001, title = "Test", id = 1)
        dao.insert(anime)

        val now = System.currentTimeMillis()
        val reason = "API unreachable"
        dao.recordAnimeUpdateAttempt(now, AnimeUpdateResult.Failure(reason))

        dao.observeLastAnimeUpdateAttemptFailureReason().test {
            val result = awaitItem()
            assertThat(result).isEqualTo(reason)
            awaitComplete()
        }
    }

    @Test
    fun `clearSchedulerState removes scheduler state`() = runTest {
        val anime = Anime(malId = 1001, title = "Test", id = 1)
        dao.insert(anime)

        dao.recordAnimeUpdateAttempt(System.currentTimeMillis(), AnimeUpdateResult.Success)
        dao.deleteAllSchedulerState()

        dao.observeLastAnimeUpdateRun().test {
            val result = awaitItem()
            assertThat(result).isNull()
            awaitComplete()
        }
    }
}
