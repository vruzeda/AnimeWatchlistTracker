package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeImagesDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.GenreDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.ImageUrlDto
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import org.junit.jupiter.api.Test

class AnimeDtoMapperTest {

    @Test
    fun `toDomainModel maps all fields correctly`() {
        val dto = AnimeDataDto(
            malId = 21,
            title = "One Punch Man",
            images = AnimeImagesDto(
                jpg = ImageUrlDto(
                    largeImageUrl = "https://cdn.myanimelist.net/large.jpg",
                    imageUrl = "https://cdn.myanimelist.net/normal.jpg"
                )
            ),
            synopsis = "A hero who defeats enemies with one punch.",
            episodes = 12,
            score = 8.7,
            genres = listOf(GenreDto(name = "Action"), GenreDto(name = "Comedy"))
        )

        val anime = dto.toDomainModel()

        assertThat(anime.id).isEqualTo(0L)
        assertThat(anime.malId).isEqualTo(21)
        assertThat(anime.title).isEqualTo("One Punch Man")
        assertThat(anime.imageUrl).isEqualTo("https://cdn.myanimelist.net/large.jpg")
        assertThat(anime.synopsis).isEqualTo("A hero who defeats enemies with one punch.")
        assertThat(anime.episodeCount).isEqualTo(12)
        assertThat(anime.currentEpisode).isEqualTo(0)
        assertThat(anime.score).isEqualTo(8.7)
        assertThat(anime.userRating).isNull()
        assertThat(anime.status).isEqualTo(WatchStatus.PLAN_TO_WATCH)
        assertThat(anime.genres).containsExactly("Action", "Comedy")
    }

    @Test
    fun `toDomainModel prefers largeImageUrl over imageUrl`() {
        val dto = AnimeDataDto(
            malId = 1,
            title = "Test",
            images = AnimeImagesDto(
                jpg = ImageUrlDto(
                    largeImageUrl = "https://large.jpg",
                    imageUrl = "https://normal.jpg"
                )
            )
        )

        val anime = dto.toDomainModel()

        assertThat(anime.imageUrl).isEqualTo("https://large.jpg")
    }

    @Test
    fun `toDomainModel falls back to imageUrl when largeImageUrl is null`() {
        val dto = AnimeDataDto(
            malId = 1,
            title = "Test",
            images = AnimeImagesDto(
                jpg = ImageUrlDto(
                    largeImageUrl = null,
                    imageUrl = "https://normal.jpg"
                )
            )
        )

        val anime = dto.toDomainModel()

        assertThat(anime.imageUrl).isEqualTo("https://normal.jpg")
    }

    @Test
    fun `toDomainModel handles null images`() {
        val dto = AnimeDataDto(
            malId = 1,
            title = "Test",
            images = null
        )

        val anime = dto.toDomainModel()

        assertThat(anime.imageUrl).isNull()
    }

    @Test
    fun `toDomainModel handles null jpg`() {
        val dto = AnimeDataDto(
            malId = 1,
            title = "Test",
            images = AnimeImagesDto(jpg = null)
        )

        val anime = dto.toDomainModel()

        assertThat(anime.imageUrl).isNull()
    }

    @Test
    fun `toDomainModel handles null genres`() {
        val dto = AnimeDataDto(
            malId = 1,
            title = "Test",
            genres = null
        )

        val anime = dto.toDomainModel()

        assertThat(anime.genres).isEmpty()
    }

    @Test
    fun `toDomainModel handles null optional fields`() {
        val dto = AnimeDataDto(
            malId = 1,
            title = "Test",
            images = null,
            synopsis = null,
            episodes = null,
            score = null,
            genres = null
        )

        val anime = dto.toDomainModel()

        assertThat(anime.imageUrl).isNull()
        assertThat(anime.synopsis).isNull()
        assertThat(anime.episodeCount).isNull()
        assertThat(anime.score).isNull()
        assertThat(anime.genres).isEmpty()
    }
}
