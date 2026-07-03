package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
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
class EpisodeInfoRoomDaoTest {

    private lateinit var database: AnimeDatabase
    private lateinit var dao: EpisodeInfoRoomDao

    @BeforeEach
    fun setup() {
        database = RoomDatabaseTestHelper.createInMemoryDatabase()
        dao = database.episodeInfoDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun `upsertEpisodes inserts episodes for malId`() = runTest {
        val malId = 1001
        val episodes = listOf(
            EpisodeInfo(number = 1, title = "Ep 1", aired = "2024-01-01", isFiller = false, isRecap = false),
            EpisodeInfo(number = 2, title = "Ep 2", aired = "2024-01-08", isFiller = false, isRecap = false),
            EpisodeInfo(number = 3, title = "Ep 3", aired = "2024-01-15", isFiller = true, isRecap = false)
        )

        dao.upsertEpisodes(malId, episodes)

        val retrieved = dao.getEpisodes(malId)
        assertThat(retrieved).hasSize(3)
        assertThat(retrieved[0].number).isEqualTo(1)
        assertThat(retrieved[0].title).isEqualTo("Ep 1")
        assertThat(retrieved[2].isFiller).isTrue()
    }

    @Test
    fun `getEpisodes returns episodes ordered by number`() = runTest {
        val malId = 2001
        val episodes = listOf(
            EpisodeInfo(number = 3, title = "Ep 3"),
            EpisodeInfo(number = 1, title = "Ep 1"),
            EpisodeInfo(number = 2, title = "Ep 2")
        )

        dao.upsertEpisodes(malId, episodes)

        val retrieved = dao.getEpisodes(malId)
        assertThat(retrieved.map { it.number }).containsExactly(1, 2, 3).inOrder()
    }

    @Test
    fun `upsertEpisodes replaces existing episodes for same malId and number`() = runTest {
        val malId = 3001
        val episode1 = EpisodeInfo(number = 1, title = "Original", aired = "2024-01-01")
        val episode2 = EpisodeInfo(number = 2, title = "Ep 2", aired = "2024-01-08")

        dao.upsertEpisodes(malId, listOf(episode1, episode2))

        val updated = EpisodeInfo(number = 1, title = "Updated", aired = "2024-02-01", isRecap = true)
        dao.upsertEpisodes(malId, listOf(updated))

        val retrieved = dao.getEpisodes(malId)
        assertThat(retrieved).hasSize(2)
        assertThat(retrieved[0].title).isEqualTo("Updated")
        assertThat(retrieved[0].isRecap).isTrue()
    }

    @Test
    fun `getEpisodes returns empty list for non-existent malId`() = runTest {
        val retrieved = dao.getEpisodes(9999)
        assertThat(retrieved).isEmpty()
    }

    @Test
    fun `episodes for different malIds are kept separate`() = runTest {
        val malId1 = 1001
        val malId2 = 2001

        dao.upsertEpisodes(malId1, listOf(EpisodeInfo(number = 1, title = "Show1-Ep1")))
        dao.upsertEpisodes(malId2, listOf(EpisodeInfo(number = 1, title = "Show2-Ep1")))

        val result1 = dao.getEpisodes(malId1)
        val result2 = dao.getEpisodes(malId2)

        assertThat(result1[0].title).isEqualTo("Show1-Ep1")
        assertThat(result2[0].title).isEqualTo("Show2-Ep1")
    }

    @Test
    fun `episode fields roundtrip correctly`() = runTest {
        val malId = 5001
        val episode = EpisodeInfo(
            number = 5,
            title = "Complex Title",
            aired = "2024-03-15",
            isFiller = true,
            isRecap = true
        )

        dao.upsertEpisodes(malId, listOf(episode))

        val retrieved = dao.getEpisodes(malId)
        assertThat(retrieved[0]).isEqualTo(episode)
    }

    @Test
    fun `episodes with null fields store correctly`() = runTest {
        val malId = 6001
        val episode = EpisodeInfo(
            number = 1,
            title = null,
            aired = null,
            isFiller = false,
            isRecap = false
        )

        dao.upsertEpisodes(malId, listOf(episode))

        val retrieved = dao.getEpisodes(malId)
        assertThat(retrieved[0].title).isNull()
        assertThat(retrieved[0].aired).isNull()
    }
}
