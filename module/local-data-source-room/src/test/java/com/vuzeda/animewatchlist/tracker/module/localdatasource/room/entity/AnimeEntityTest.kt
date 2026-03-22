package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AnimeEntityTest {

    @Test
    fun `toDomainModel maps blank genres string to empty list`() {
        val entity = AnimeEntity(
            title = "Test",
            genres = ""
        )

        val result = entity.toDomainModel()

        assertThat(result.genres).isEmpty()
    }

    @Test
    fun `toDomainModel maps genres string with whitespace around comma entries`() {
        val entity = AnimeEntity(
            title = "Test",
            genres = "Action, Drama, Sci-Fi"
        )

        val result = entity.toDomainModel()

        assertThat(result.genres).containsExactly("Action", "Drama", "Sci-Fi").inOrder()
    }

    @Test
    fun `toDomainModel maps valid NotificationType`() {
        NotificationType.entries.forEach { notificationType ->
            val entity = AnimeEntity(
                title = "Test",
                genres = "",
                notificationType = notificationType.name
            )

            assertThat(entity.toDomainModel().notificationType).isEqualTo(notificationType)
        }
    }

    @Test
    fun `toDomainModel falls back to NONE for unknown notificationType`() {
        val entity = AnimeEntity(
            title = "Test",
            genres = "",
            notificationType = "UNKNOWN_TYPE"
        )

        val result = entity.toDomainModel()

        assertThat(result.notificationType).isEqualTo(NotificationType.NONE)
    }

    @Test
    fun `toDomainModel maps all fields correctly`() {
        val checkDate = LocalDate.of(2026, 3, 15)
        val entity = AnimeEntity(
            id = 42L,
            title = "Attack on Titan",
            titleEnglish = "Attack on Titan",
            titleJapanese = "Shingeki no Kyojin",
            imageUrl = "https://example.com/aot.jpg",
            synopsis = "Humanity vs titans",
            genres = "Action,Drama",
            userRating = 9,
            notificationType = "BOTH",
            lastSeasonCheckDate = checkDate,
            addedAt = 1000L
        )

        val result = entity.toDomainModel()

        assertThat(result.id).isEqualTo(42L)
        assertThat(result.title).isEqualTo("Attack on Titan")
        assertThat(result.titleEnglish).isEqualTo("Attack on Titan")
        assertThat(result.titleJapanese).isEqualTo("Shingeki no Kyojin")
        assertThat(result.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(result.synopsis).isEqualTo("Humanity vs titans")
        assertThat(result.genres).containsExactly("Action", "Drama").inOrder()
        assertThat(result.userRating).isEqualTo(9)
        assertThat(result.notificationType).isEqualTo(NotificationType.BOTH)
        assertThat(result.lastSeasonCheckDate).isEqualTo(checkDate)
        assertThat(result.addedAt).isEqualTo(1000L)
    }

    @Test
    fun `toEntity maps Anime to AnimeEntity`() {
        val checkDate = LocalDate.of(2026, 3, 15)
        val anime = Anime(
            id = 5L,
            title = "One Piece",
            titleEnglish = "One Piece",
            titleJapanese = "ワンピース",
            imageUrl = "https://example.com/op.jpg",
            synopsis = "Pirates and treasure",
            genres = listOf("Adventure", "Comedy"),
            status = WatchStatus.COMPLETED,
            userRating = 10,
            notificationType = NotificationType.NEW_EPISODES,
            lastSeasonCheckDate = checkDate,
            addedAt = 2000L
        )

        val result = anime.toEntity()

        assertThat(result.id).isEqualTo(5L)
        assertThat(result.title).isEqualTo("One Piece")
        assertThat(result.titleEnglish).isEqualTo("One Piece")
        assertThat(result.titleJapanese).isEqualTo("ワンピース")
        assertThat(result.imageUrl).isEqualTo("https://example.com/op.jpg")
        assertThat(result.synopsis).isEqualTo("Pirates and treasure")
        assertThat(result.genres).isEqualTo("Adventure,Comedy")
        assertThat(result.userRating).isEqualTo(10)
        assertThat(result.notificationType).isEqualTo("NEW_EPISODES")
        assertThat(result.lastSeasonCheckDate).isEqualTo(checkDate)
        assertThat(result.addedAt).isEqualTo(2000L)
    }

    @Test
    fun `toEntity joins empty genres list as empty string`() {
        val anime = Anime(title = "Test", genres = emptyList())

        val result = anime.toEntity()

        assertThat(result.genres).isEqualTo("")
    }
}
