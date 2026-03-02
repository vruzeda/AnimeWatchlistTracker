package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.local.entity.SeasonEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import org.junit.jupiter.api.Test

class SeasonEntityMapperTest {

    @Test
    fun `toDomainModel maps all fields correctly`() {
        val entity = SeasonEntity(
            id = 1L,
            animeId = 10L,
            malId = 16498,
            title = "Attack on Titan",
            imageUrl = "https://example.com/aot.jpg",
            type = "TV",
            episodeCount = 25,
            currentEpisode = 12,
            score = 8.5,
            orderIndex = 0,
            airingStatus = "Finished Airing",
            lastCheckedAiredEpisodeCount = 25,
            isEpisodeNotificationsEnabled = true
        )

        val season = entity.toDomainModel()

        assertThat(season.id).isEqualTo(1L)
        assertThat(season.animeId).isEqualTo(10L)
        assertThat(season.malId).isEqualTo(16498)
        assertThat(season.title).isEqualTo("Attack on Titan")
        assertThat(season.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(season.type).isEqualTo("TV")
        assertThat(season.episodeCount).isEqualTo(25)
        assertThat(season.currentEpisode).isEqualTo(12)
        assertThat(season.score).isEqualTo(8.5)
        assertThat(season.orderIndex).isEqualTo(0)
        assertThat(season.airingStatus).isEqualTo("Finished Airing")
        assertThat(season.lastCheckedAiredEpisodeCount).isEqualTo(25)
        assertThat(season.isEpisodeNotificationsEnabled).isTrue()
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val season = Season(
            id = 1L,
            animeId = 10L,
            malId = 16498,
            title = "Attack on Titan",
            imageUrl = "https://example.com/aot.jpg",
            type = "TV",
            episodeCount = 25,
            currentEpisode = 12,
            score = 8.5,
            orderIndex = 0,
            airingStatus = "Finished Airing",
            lastCheckedAiredEpisodeCount = 25,
            isEpisodeNotificationsEnabled = true
        )

        val entity = season.toEntity()

        assertThat(entity.id).isEqualTo(1L)
        assertThat(entity.animeId).isEqualTo(10L)
        assertThat(entity.malId).isEqualTo(16498)
        assertThat(entity.title).isEqualTo("Attack on Titan")
        assertThat(entity.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(entity.type).isEqualTo("TV")
        assertThat(entity.episodeCount).isEqualTo(25)
        assertThat(entity.currentEpisode).isEqualTo(12)
        assertThat(entity.score).isEqualTo(8.5)
        assertThat(entity.orderIndex).isEqualTo(0)
        assertThat(entity.airingStatus).isEqualTo("Finished Airing")
        assertThat(entity.lastCheckedAiredEpisodeCount).isEqualTo(25)
        assertThat(entity.isEpisodeNotificationsEnabled).isTrue()
    }

    @Test
    fun `handles null optional fields`() {
        val entity = SeasonEntity(
            id = 1L,
            animeId = 10L,
            malId = 100,
            title = "Test",
            imageUrl = null,
            episodeCount = null,
            score = null,
            airingStatus = null,
            lastCheckedAiredEpisodeCount = null
        )

        val season = entity.toDomainModel()

        assertThat(season.imageUrl).isNull()
        assertThat(season.episodeCount).isNull()
        assertThat(season.score).isNull()
        assertThat(season.airingStatus).isNull()
        assertThat(season.lastCheckedAiredEpisodeCount).isNull()
    }

    @Test
    fun `round trip preserves data`() {
        val original = Season(
            id = 5L,
            animeId = 10L,
            malId = 16498,
            title = "Attack on Titan",
            imageUrl = "https://example.com/aot.jpg",
            type = "TV",
            episodeCount = 25,
            currentEpisode = 12,
            score = 8.5,
            orderIndex = 0,
            airingStatus = "Finished Airing",
            lastCheckedAiredEpisodeCount = 25,
            isEpisodeNotificationsEnabled = true
        )

        val roundTripped = original.toEntity().toDomainModel()

        assertThat(roundTripped).isEqualTo(original)
    }
}
