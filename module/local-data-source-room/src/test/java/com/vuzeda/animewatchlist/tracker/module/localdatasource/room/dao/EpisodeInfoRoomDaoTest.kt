package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
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
class EpisodeInfoRoomDaoTest {

    private lateinit var database: AnimeDatabase
    private lateinit var dao: EpisodeInfoRoomDao

    @Before
    fun setup() {
        database = RoomDatabaseTestHelper.createInMemoryDatabase()
        dao = database.episodeInfoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun episode(number: Int, title: String? = "Ep $number") = EpisodeInfo(
        number = number,
        title = title,
        aired = "2026-01-0$number",
        isFiller = false,
        isRecap = false
    )

    @Test
    fun `upsertEpisodes and getEpisodes round-trips episodes ordered by number`() = runTest {
        dao.upsertEpisodes(malId = 100, episodes = listOf(episode(2), episode(1)))

        val episodes = dao.getEpisodes(100)

        assertThat(episodes.map { it.number }).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `getEpisodes returns only episodes for the requested malId`() = runTest {
        dao.upsertEpisodes(malId = 100, episodes = listOf(episode(1)))
        dao.upsertEpisodes(malId = 200, episodes = listOf(episode(1), episode(2)))

        assertThat(dao.getEpisodes(100)).hasSize(1)
        assertThat(dao.getEpisodes(200)).hasSize(2)
    }

    @Test
    fun `getEpisodes returns empty list for unknown malId`() = runTest {
        assertThat(dao.getEpisodes(999)).isEmpty()
    }

    @Test
    fun `upsertEpisodes replaces an existing episode with the same number`() = runTest {
        dao.upsertEpisodes(malId = 100, episodes = listOf(episode(1, title = "Old title")))

        dao.upsertEpisodes(malId = 100, episodes = listOf(episode(1, title = "New title")))

        val episodes = dao.getEpisodes(100)
        assertThat(episodes).hasSize(1)
        assertThat(episodes.first().title).isEqualTo("New title")
    }

    @Test
    fun `episode fields round-trip including flags`() = runTest {
        val fillerRecap = EpisodeInfo(
            number = 5,
            title = "Recap special",
            aired = "2026-02-14",
            isFiller = true,
            isRecap = true
        )

        dao.upsertEpisodes(malId = 100, episodes = listOf(fillerRecap))

        assertThat(dao.getEpisodes(100).first()).isEqualTo(fillerRecap)
    }

    @Test
    fun `null title and aired round-trip`() = runTest {
        val tbaEpisode = EpisodeInfo(number = 7, title = null, aired = null, isFiller = false, isRecap = false)

        dao.upsertEpisodes(malId = 100, episodes = listOf(tbaEpisode))

        assertThat(dao.getEpisodes(100).first()).isEqualTo(tbaEpisode)
    }

    @Test
    fun `deleteEpisodesNotInWatchlist keeps episodes whose malId has a season`() = runTest {
        val animeId = database.animeDao().insert(Anime(title = "Parent"))
        database.seasonDao().insertAll(listOf(Season(animeId = animeId, malId = 100, title = "Tracked")))
        dao.upsertEpisodes(malId = 100, episodes = listOf(episode(1)))
        dao.upsertEpisodes(malId = 200, episodes = listOf(episode(1)))

        dao.deleteEpisodesNotInWatchlist()

        assertThat(dao.getEpisodes(100)).hasSize(1)
        assertThat(dao.getEpisodes(200)).isEmpty()
    }
}
