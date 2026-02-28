package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import org.junit.jupiter.api.Test

class AnimeEntityMapperTest {

    @Test
    fun `toDomainModel maps all fields correctly`() {
        val entity = AnimeEntity(
            id = 1L,
            malId = 21,
            title = "One Punch Man",
            imageUrl = "https://example.com/opm.jpg",
            synopsis = "A hero who defeats enemies with one punch.",
            episodeCount = 12,
            currentEpisode = 6,
            score = 8.7,
            userRating = 9,
            status = "WATCHING",
            genres = "Action,Comedy"
        )

        val anime = entity.toDomainModel()

        assertThat(anime.id).isEqualTo(1L)
        assertThat(anime.malId).isEqualTo(21)
        assertThat(anime.title).isEqualTo("One Punch Man")
        assertThat(anime.imageUrl).isEqualTo("https://example.com/opm.jpg")
        assertThat(anime.synopsis).isEqualTo("A hero who defeats enemies with one punch.")
        assertThat(anime.episodeCount).isEqualTo(12)
        assertThat(anime.currentEpisode).isEqualTo(6)
        assertThat(anime.score).isEqualTo(8.7)
        assertThat(anime.userRating).isEqualTo(9)
        assertThat(anime.status).isEqualTo(WatchStatus.WATCHING)
        assertThat(anime.genres).containsExactly("Action", "Comedy")
    }

    @Test
    fun `toDomainModel handles empty genres`() {
        val entity = AnimeEntity(
            id = 1L,
            title = "Test",
            status = "COMPLETED",
            genres = ""
        )

        val anime = entity.toDomainModel()

        assertThat(anime.genres).isEmpty()
    }

    @Test
    fun `toDomainModel handles null optional fields`() {
        val entity = AnimeEntity(
            id = 1L,
            malId = null,
            title = "Test",
            imageUrl = null,
            synopsis = null,
            episodeCount = null,
            currentEpisode = 0,
            score = null,
            userRating = null,
            status = "PLAN_TO_WATCH",
            genres = ""
        )

        val anime = entity.toDomainModel()

        assertThat(anime.malId).isNull()
        assertThat(anime.imageUrl).isNull()
        assertThat(anime.synopsis).isNull()
        assertThat(anime.episodeCount).isNull()
        assertThat(anime.score).isNull()
        assertThat(anime.userRating).isNull()
    }

    @Test
    fun `toDomainModel defaults to PLAN_TO_WATCH for unknown status`() {
        val entity = AnimeEntity(
            id = 1L,
            title = "Test",
            status = "UNKNOWN_STATUS",
            genres = ""
        )

        val anime = entity.toDomainModel()

        assertThat(anime.status).isEqualTo(WatchStatus.PLAN_TO_WATCH)
    }

    @Test
    fun `toDomainModel trims whitespace from genres`() {
        val entity = AnimeEntity(
            id = 1L,
            title = "Test",
            status = "WATCHING",
            genres = "Action , Comedy , Drama"
        )

        val anime = entity.toDomainModel()

        assertThat(anime.genres).containsExactly("Action", "Comedy", "Drama")
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val anime = Anime(
            id = 1L,
            malId = 21,
            title = "One Punch Man",
            imageUrl = "https://example.com/opm.jpg",
            synopsis = "A hero who defeats enemies with one punch.",
            episodeCount = 12,
            currentEpisode = 6,
            score = 8.7,
            userRating = 9,
            status = WatchStatus.WATCHING,
            genres = listOf("Action", "Comedy")
        )

        val entity = anime.toEntity()

        assertThat(entity.id).isEqualTo(1L)
        assertThat(entity.malId).isEqualTo(21)
        assertThat(entity.title).isEqualTo("One Punch Man")
        assertThat(entity.imageUrl).isEqualTo("https://example.com/opm.jpg")
        assertThat(entity.synopsis).isEqualTo("A hero who defeats enemies with one punch.")
        assertThat(entity.episodeCount).isEqualTo(12)
        assertThat(entity.currentEpisode).isEqualTo(6)
        assertThat(entity.score).isEqualTo(8.7)
        assertThat(entity.userRating).isEqualTo(9)
        assertThat(entity.status).isEqualTo("WATCHING")
        assertThat(entity.genres).isEqualTo("Action,Comedy")
    }

    @Test
    fun `toEntity handles empty genres list`() {
        val anime = Anime(
            id = 1L,
            title = "Test",
            genres = emptyList()
        )

        val entity = anime.toEntity()

        assertThat(entity.genres).isEmpty()
    }

    @Test
    fun `round trip preserves data`() {
        val original = Anime(
            id = 5L,
            malId = 100,
            title = "Attack on Titan",
            imageUrl = "https://example.com/aot.jpg",
            synopsis = "Humanity fights titans.",
            episodeCount = 25,
            currentEpisode = 10,
            score = 9.0,
            userRating = 10,
            status = WatchStatus.WATCHING,
            genres = listOf("Action", "Drama", "Fantasy")
        )

        val roundTripped = original.toEntity().toDomainModel()

        assertThat(roundTripped).isEqualTo(original)
    }
}
