package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.RoomDatabaseTestHelper
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.AnimeDatabase
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AnimeRoomDaoTest {

    private lateinit var database: AnimeDatabase
    private lateinit var dao: AnimeRoomDao

    @Before
    fun setup() {
        database = RoomDatabaseTestHelper.createInMemoryDatabase()
        dao = database.animeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and getById round-trips all persisted fields`() = runTest {
        val anime = Anime(
            title = "Frieren",
            titleEnglish = "Frieren: Beyond Journey's End",
            genres = listOf("Adventure", "Fantasy"),
            userRating = 9,
            notificationType = NotificationType.BOTH
        )

        val id = dao.insert(anime)

        assertThat(dao.getById(id)).isEqualTo(anime.copy(id = id))
    }

    @Test
    fun `getById returns null for unknown id`() = runTest {
        assertThat(dao.getById(999L)).isNull()
    }

    @Test
    fun `observeAll emits anime ordered by title`() = runTest {
        dao.insert(Anime(title = "Zeta Gundam"))
        dao.insert(Anime(title = "Akira"))

        dao.observeAll().test {
            val titles = awaitItem().map { it.title }
            assertThat(titles).containsExactly("Akira", "Zeta Gundam").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeById emits the matching anime`() = runTest {
        val id = dao.insert(Anime(title = "Monster"))

        dao.observeById(id).test {
            assertThat(awaitItem()?.title).isEqualTo("Monster")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update persists modified fields`() = runTest {
        val id = dao.insert(Anime(title = "Original"))

        dao.update(Anime(id = id, title = "Renamed", userRating = 10))

        val updated = dao.getById(id)
        assertThat(updated?.title).isEqualTo("Renamed")
        assertThat(updated?.userRating).isEqualTo(10)
    }

    @Test
    fun `deleteById removes only the targeted anime`() = runTest {
        val keptId = dao.insert(Anime(title = "Kept"))
        val removedId = dao.insert(Anime(title = "Removed"))

        dao.deleteById(removedId)

        assertThat(dao.getById(removedId)).isNull()
        assertThat(dao.getById(keptId)).isNotNull()
    }

    @Test
    fun `deleteAll removes every anime`() = runTest {
        dao.insert(Anime(title = "One"))
        dao.insert(Anime(title = "Two"))

        dao.deleteAll()

        assertThat(dao.observeAll().first()).isEmpty()
    }

    @Test
    fun `getNotificationEnabledAnime returns only anime with notifications enabled`() = runTest {
        dao.insert(Anime(title = "Silent", notificationType = NotificationType.NONE))
        dao.insert(Anime(title = "Episodes", notificationType = NotificationType.NEW_EPISODES))
        dao.insert(Anime(title = "Both", notificationType = NotificationType.BOTH))

        val enabled = dao.getNotificationEnabledAnime().map { it.title }

        assertThat(enabled).containsExactly("Episodes", "Both")
    }

    @Test
    fun `updateNotificationType persists the new type`() = runTest {
        val id = dao.insert(Anime(title = "Show", notificationType = NotificationType.NONE))

        dao.updateNotificationType(id, NotificationType.NEW_SEASONS)

        assertThat(dao.getById(id)?.notificationType).isEqualTo(NotificationType.NEW_SEASONS)
    }

    @Test
    fun `updateLatestKnownSeasonStartDate persists the date`() = runTest {
        val id = dao.insert(Anime(title = "Show"))
        val startDate = LocalDate.of(2026, 1, 15)

        dao.updateLatestKnownSeasonStartDate(id, startDate)

        assertThat(dao.getById(id)?.latestKnownSeasonStartDate).isEqualTo(startDate)
    }

    @Test
    fun `updateLastSeasonCheckPerformedDate persists the date`() = runTest {
        val id = dao.insert(Anime(title = "Show"))
        val checkDate = LocalDate.of(2026, 7, 3)

        dao.updateLastSeasonCheckPerformedDate(id, checkDate)

        assertThat(dao.getById(id)?.lastSeasonCheckPerformedDate).isEqualTo(checkDate)
    }

    @Test
    fun `recordAnimeUpdateAttempt with success updates run and attempt state`() = runTest {
        dao.recordAnimeUpdateAttempt(epochMillis = 1_000L, result = AnimeUpdateResult.Success)

        assertThat(dao.observeLastAnimeUpdateRun().first()).isEqualTo(1_000L)
        assertThat(dao.observeLastAnimeUpdateAttemptAt().first()).isEqualTo(1_000L)
        assertThat(dao.observeLastAnimeUpdateAttemptResult().first()).isEqualTo("SUCCESS")
    }

    @Test
    fun `recordAnimeUpdateAttempt with retry keeps the last successful run timestamp`() = runTest {
        dao.recordAnimeUpdateAttempt(epochMillis = 1_000L, result = AnimeUpdateResult.Success)

        dao.recordAnimeUpdateAttempt(
            epochMillis = 2_000L,
            result = AnimeUpdateResult.WillRetry(reason = "timeout", retryCount = 2)
        )

        assertThat(dao.observeLastAnimeUpdateRun().first()).isEqualTo(1_000L)
        assertThat(dao.observeLastAnimeUpdateAttemptAt().first()).isEqualTo(2_000L)
        assertThat(dao.observeLastAnimeUpdateAttemptResult().first()).isEqualTo("WILL_RETRY")
        assertThat(dao.observeLastAnimeUpdateAttemptFailureReason().first()).isEqualTo("timeout")
        assertThat(dao.observeLastAnimeUpdateAttemptRetryCount().first()).isEqualTo(2)
    }

    @Test
    fun `recordAnimeUpdateAttempt with failure stores the reason`() = runTest {
        dao.recordAnimeUpdateAttempt(
            epochMillis = 3_000L,
            result = AnimeUpdateResult.Failure(reason = "network down")
        )

        assertThat(dao.observeLastAnimeUpdateAttemptResult().first()).isEqualTo("FAILURE")
        assertThat(dao.observeLastAnimeUpdateAttemptFailureReason().first()).isEqualTo("network down")
    }

    @Test
    fun `clearSchedulerState resets scheduler observations`() = runTest {
        dao.recordAnimeUpdateAttempt(epochMillis = 1_000L, result = AnimeUpdateResult.Success)

        dao.clearSchedulerState()

        assertThat(dao.observeLastAnimeUpdateRun().first()).isNull()
        assertThat(dao.observeLastAnimeUpdateAttemptAt().first()).isNull()
    }
}
