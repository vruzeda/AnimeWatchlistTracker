package com.vuzeda.animewatchlist.tracker.module.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.localdatasource.Season as LocalSeason
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import org.junit.jupiter.api.Test

class SeasonEntityMapperTest {

    @Test
    fun `toDomainModel maps all fields correctly`() {
        val localSeason = LocalSeason(
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

        val season = localSeason.toDomainModel()

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
    fun `toLocalModel maps all fields correctly`() {
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

        val localSeason = season.toLocalModel()

        assertThat(localSeason.id).isEqualTo(1L)
        assertThat(localSeason.animeId).isEqualTo(10L)
        assertThat(localSeason.malId).isEqualTo(16498)
        assertThat(localSeason.title).isEqualTo("Attack on Titan")
        assertThat(localSeason.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(localSeason.type).isEqualTo("TV")
        assertThat(localSeason.episodeCount).isEqualTo(25)
        assertThat(localSeason.currentEpisode).isEqualTo(12)
        assertThat(localSeason.score).isEqualTo(8.5)
        assertThat(localSeason.orderIndex).isEqualTo(0)
        assertThat(localSeason.airingStatus).isEqualTo("Finished Airing")
        assertThat(localSeason.lastCheckedAiredEpisodeCount).isEqualTo(25)
        assertThat(localSeason.isEpisodeNotificationsEnabled).isTrue()
    }

    @Test
    fun `handles null optional fields`() {
        val localSeason = LocalSeason(
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

        val season = localSeason.toDomainModel()

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

        val roundTripped = original.toLocalModel().toDomainModel()

        assertThat(roundTripped).isEqualTo(original)
    }
}
