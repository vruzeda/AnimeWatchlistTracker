package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.local.Anime as LocalAnime
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.NotificationType
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import org.junit.jupiter.api.Test

class AnimeEntityMapperTest {

    @Test
    fun `toDomainModel maps all fields correctly`() {
        val localAnime = LocalAnime(
            id = 1L,
            title = "Attack on Titan",
            imageUrl = "https://example.com/aot.jpg",
            synopsis = "Humanity fights titans.",
            genres = "Action,Drama",
            status = "WATCHING",
            userRating = 9,
            notificationType = "BOTH",
            addedAt = 1000L
        )

        val anime = localAnime.toDomainModel()

        assertThat(anime.id).isEqualTo(1L)
        assertThat(anime.title).isEqualTo("Attack on Titan")
        assertThat(anime.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(anime.synopsis).isEqualTo("Humanity fights titans.")
        assertThat(anime.genres).containsExactly("Action", "Drama")
        assertThat(anime.status).isEqualTo(WatchStatus.WATCHING)
        assertThat(anime.userRating).isEqualTo(9)
        assertThat(anime.notificationType).isEqualTo(NotificationType.BOTH)
        assertThat(anime.isNotificationsEnabled).isTrue()
        assertThat(anime.addedAt).isEqualTo(1000L)
    }

    @Test
    fun `toDomainModel handles empty genres`() {
        val localAnime = LocalAnime(
            id = 1L,
            title = "Test",
            status = "COMPLETED",
            genres = ""
        )

        val anime = localAnime.toDomainModel()

        assertThat(anime.genres).isEmpty()
    }

    @Test
    fun `toDomainModel handles null optional fields`() {
        val localAnime = LocalAnime(
            id = 1L,
            title = "Test",
            imageUrl = null,
            synopsis = null,
            userRating = null,
            status = "PLAN_TO_WATCH",
            genres = ""
        )

        val anime = localAnime.toDomainModel()

        assertThat(anime.imageUrl).isNull()
        assertThat(anime.synopsis).isNull()
        assertThat(anime.userRating).isNull()
    }

    @Test
    fun `toDomainModel defaults to PLAN_TO_WATCH for unknown status`() {
        val localAnime = LocalAnime(
            id = 1L,
            title = "Test",
            status = "UNKNOWN_STATUS",
            genres = ""
        )

        val anime = localAnime.toDomainModel()

        assertThat(anime.status).isEqualTo(WatchStatus.PLAN_TO_WATCH)
    }

    @Test
    fun `toDomainModel trims whitespace from genres`() {
        val localAnime = LocalAnime(
            id = 1L,
            title = "Test",
            status = "WATCHING",
            genres = "Action , Comedy , Drama"
        )

        val anime = localAnime.toDomainModel()

        assertThat(anime.genres).containsExactly("Action", "Comedy", "Drama")
    }

    @Test
    fun `toLocalModel maps all fields correctly`() {
        val anime = Anime(
            id = 1L,
            title = "Attack on Titan",
            imageUrl = "https://example.com/aot.jpg",
            synopsis = "Humanity fights titans.",
            genres = listOf("Action", "Drama"),
            status = WatchStatus.WATCHING,
            userRating = 9,
            notificationType = NotificationType.BOTH,
            addedAt = 1000L
        )

        val localAnime = anime.toLocalModel()

        assertThat(localAnime.id).isEqualTo(1L)
        assertThat(localAnime.title).isEqualTo("Attack on Titan")
        assertThat(localAnime.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(localAnime.synopsis).isEqualTo("Humanity fights titans.")
        assertThat(localAnime.genres).isEqualTo("Action,Drama")
        assertThat(localAnime.status).isEqualTo("WATCHING")
        assertThat(localAnime.userRating).isEqualTo(9)
        assertThat(localAnime.notificationType).isEqualTo("BOTH")
        assertThat(localAnime.addedAt).isEqualTo(1000L)
    }

    @Test
    fun `toLocalModel handles empty genres list`() {
        val anime = Anime(
            id = 1L,
            title = "Test",
            genres = emptyList()
        )

        val localAnime = anime.toLocalModel()

        assertThat(localAnime.genres).isEmpty()
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
            notificationType = NotificationType.BOTH,
            addedAt = 1000L
        )

        val roundTripped = original.toLocalModel().toDomainModel()

        assertThat(roundTripped).isEqualTo(original)
    }
}
