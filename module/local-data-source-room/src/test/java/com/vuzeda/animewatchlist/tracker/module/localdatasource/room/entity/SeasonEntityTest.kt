package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import java.time.LocalDate
import org.junit.jupiter.api.Test

class SeasonEntityTest {

    @Test
    fun `toDomainModel maps all fields correctly`() {
        val checkDate = LocalDate.of(2026, 3, 15)
        val entity = SeasonEntity(
            id = 10L,
            animeId = 2L,
            malId = 16498,
            title = "Shingeki no Kyojin",
            titleEnglish = "Attack on Titan",
            titleJapanese = "進撃の巨人",
            imageUrl = "https://example.com/aot.jpg",
            type = "TV",
            episodeCount = 25,
            currentEpisode = 12,
            status = "WATCHING",
            score = 8.54,
            orderIndex = 0,
            airingStatus = "Finished Airing",
            lastCheckedAiredEpisodeCount = 25,
            lastEpisodeCheckDate = checkDate,
            isEpisodeNotificationsEnabled = true,
            isInWatchlist = false
        )

        val result = entity.toDomainModel()

        assertThat(result.id).isEqualTo(10L)
        assertThat(result.animeId).isEqualTo(2L)
        assertThat(result.malId).isEqualTo(16498)
        assertThat(result.title).isEqualTo("Shingeki no Kyojin")
        assertThat(result.titleEnglish).isEqualTo("Attack on Titan")
        assertThat(result.titleJapanese).isEqualTo("進撃の巨人")
        assertThat(result.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(result.type).isEqualTo("TV")
        assertThat(result.episodeCount).isEqualTo(25)
        assertThat(result.currentEpisode).isEqualTo(12)
        assertThat(result.status).isEqualTo(WatchStatus.WATCHING)
        assertThat(result.score).isEqualTo(8.54)
        assertThat(result.orderIndex).isEqualTo(0)
        assertThat(result.airingStatus).isEqualTo("Finished Airing")
        assertThat(result.lastCheckedAiredEpisodeCount).isEqualTo(25)
        assertThat(result.lastEpisodeCheckDate).isEqualTo(checkDate)
        assertThat(result.isEpisodeNotificationsEnabled).isTrue()
        assertThat(result.isInWatchlist).isFalse()
    }

    @Test
    fun `toDomainModel maps valid WatchStatus`() {
        WatchStatus.entries.forEach { watchStatus ->
            val entity = SeasonEntity(animeId = 1L, malId = 100, title = "Test", status = watchStatus.name)

            assertThat(entity.toDomainModel().status).isEqualTo(watchStatus)
        }
    }

    @Test
    fun `toDomainModel falls back to PLAN_TO_WATCH for unknown status`() {
        val entity = SeasonEntity(animeId = 1L, malId = 100, title = "Test", status = "UNKNOWN_STATUS")

        val result = entity.toDomainModel()

        assertThat(result.status).isEqualTo(WatchStatus.PLAN_TO_WATCH)
    }

    @Test
    fun `toDomainModel handles null optional fields`() {
        val entity = SeasonEntity(
            animeId = 1L,
            malId = 100,
            title = "Minimal",
            titleEnglish = null,
            titleJapanese = null,
            imageUrl = null,
            episodeCount = null,
            score = null,
            airingStatus = null,
            lastCheckedAiredEpisodeCount = null,
            lastEpisodeCheckDate = null
        )

        val result = entity.toDomainModel()

        assertThat(result.titleEnglish).isNull()
        assertThat(result.titleJapanese).isNull()
        assertThat(result.imageUrl).isNull()
        assertThat(result.episodeCount).isNull()
        assertThat(result.score).isNull()
        assertThat(result.airingStatus).isNull()
        assertThat(result.lastCheckedAiredEpisodeCount).isNull()
        assertThat(result.lastEpisodeCheckDate).isNull()
    }

    @Test
    fun `toEntity maps Season to SeasonEntity`() {
        val checkDate = LocalDate.of(2026, 3, 15)
        val season = Season(
            id = 7L,
            animeId = 3L,
            malId = 16498,
            title = "Attack on Titan",
            titleEnglish = "Attack on Titan",
            titleJapanese = "進撃の巨人",
            imageUrl = "https://example.com/aot.jpg",
            type = "TV",
            episodeCount = 25,
            currentEpisode = 5,
            status = WatchStatus.COMPLETED,
            score = 8.5,
            orderIndex = 1,
            airingStatus = "Finished Airing",
            lastCheckedAiredEpisodeCount = 10,
            lastEpisodeCheckDate = checkDate,
            isEpisodeNotificationsEnabled = true,
            isInWatchlist = false
        )

        val result = season.toEntity()

        assertThat(result.id).isEqualTo(7L)
        assertThat(result.animeId).isEqualTo(3L)
        assertThat(result.malId).isEqualTo(16498)
        assertThat(result.title).isEqualTo("Attack on Titan")
        assertThat(result.titleEnglish).isEqualTo("Attack on Titan")
        assertThat(result.titleJapanese).isEqualTo("進撃の巨人")
        assertThat(result.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(result.type).isEqualTo("TV")
        assertThat(result.episodeCount).isEqualTo(25)
        assertThat(result.currentEpisode).isEqualTo(5)
        assertThat(result.status).isEqualTo("COMPLETED")
        assertThat(result.score).isEqualTo(8.5)
        assertThat(result.orderIndex).isEqualTo(1)
        assertThat(result.airingStatus).isEqualTo("Finished Airing")
        assertThat(result.lastCheckedAiredEpisodeCount).isEqualTo(10)
        assertThat(result.lastEpisodeCheckDate).isEqualTo(checkDate)
        assertThat(result.isEpisodeNotificationsEnabled).isTrue()
        assertThat(result.isInWatchlist).isFalse()
    }
}
