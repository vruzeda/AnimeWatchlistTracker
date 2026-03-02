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
            title = "Attack on Titan",
            imageUrl = "https://example.com/aot.jpg",
            synopsis = "Humanity fights titans.",
            genres = "Action,Drama",
            status = "WATCHING",
            userRating = 9,
        isNotificationsEnabled = true,
            addedAt = 1000L
        )

        val anime = entity.toDomainModel()

        assertThat(anime.id).isEqualTo(1L)
        assertThat(anime.title).isEqualTo("Attack on Titan")
        assertThat(anime.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(anime.synopsis).isEqualTo("Humanity fights titans.")
        assertThat(anime.genres).containsExactly("Action", "Drama")
        assertThat(anime.status).isEqualTo(WatchStatus.WATCHING)
        assertThat(anime.userRating).isEqualTo(9)
        assertThat(anime.isNotificationsEnabled).isTrue()
        assertThat(anime.addedAt).isEqualTo(1000L)
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
            title = "Test",
            imageUrl = null,
            synopsis = null,
            userRating = null,
            status = "PLAN_TO_WATCH",
            genres = ""
        )

        val anime = entity.toDomainModel()

        assertThat(anime.imageUrl).isNull()
        assertThat(anime.synopsis).isNull()
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
            title = "Attack on Titan",
            imageUrl = "https://example.com/aot.jpg",
            synopsis = "Humanity fights titans.",
            genres = listOf("Action", "Drama"),
            status = WatchStatus.WATCHING,
            userRating = 9,
            isNotificationsEnabled = true,
            addedAt = 1000L
        )

        val entity = anime.toEntity()

        assertThat(entity.id).isEqualTo(1L)
        assertThat(entity.title).isEqualTo("Attack on Titan")
        assertThat(entity.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(entity.synopsis).isEqualTo("Humanity fights titans.")
        assertThat(entity.genres).isEqualTo("Action,Drama")
        assertThat(entity.status).isEqualTo("WATCHING")
        assertThat(entity.userRating).isEqualTo(9)
        assertThat(entity.isNotificationsEnabled).isTrue()
        assertThat(entity.addedAt).isEqualTo(1000L)
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
            title = "Attack on Titan",
            imageUrl = "https://example.com/aot.jpg",
            synopsis = "Humanity fights titans.",
            genres = listOf("Action", "Drama", "Fantasy"),
            status = WatchStatus.WATCHING,
            userRating = 10,
            isNotificationsEnabled = true,
            addedAt = 1000L
        )

        val roundTripped = original.toEntity().toDomainModel()

        assertThat(roundTripped).isEqualTo(original)
    }
}
