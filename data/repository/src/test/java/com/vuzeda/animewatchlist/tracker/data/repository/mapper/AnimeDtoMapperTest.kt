package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeImagesDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.GenreDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.ImageUrlDto
import org.junit.jupiter.api.Test

class AnimeDtoMapperTest {

    @Test
    fun `toSearchResult maps all fields correctly`() {
        val dto = AnimeDataDto(
            malId = 21,
            title = "One Punch Man",
            type = "TV",
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

        val result = dto.toSearchResult()

        assertThat(result.malId).isEqualTo(21)
        assertThat(result.title).isEqualTo("One Punch Man")
        assertThat(result.imageUrl).isEqualTo("https://cdn.myanimelist.net/large.jpg")
        assertThat(result.synopsis).isEqualTo("A hero who defeats enemies with one punch.")
        assertThat(result.episodeCount).isEqualTo(12)
        assertThat(result.score).isEqualTo(8.7)
        assertThat(result.type).isEqualTo("TV")
        assertThat(result.genres).containsExactly("Action", "Comedy")
    }

    @Test
    fun `toSearchResult prefers largeImageUrl over imageUrl`() {
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

        assertThat(dto.toSearchResult().imageUrl).isEqualTo("https://large.jpg")
    }

    @Test
    fun `toSearchResult falls back to imageUrl when largeImageUrl is null`() {
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

        assertThat(dto.toSearchResult().imageUrl).isEqualTo("https://normal.jpg")
    }

    @Test
    fun `toSearchResult handles null images`() {
        val dto = AnimeDataDto(malId = 1, title = "Test", images = null)

        assertThat(dto.toSearchResult().imageUrl).isNull()
    }

    @Test
    fun `toSearchResult handles null genres`() {
        val dto = AnimeDataDto(malId = 1, title = "Test", genres = null)

        assertThat(dto.toSearchResult().genres).isEmpty()
    }

    @Test
    fun `toSearchResult handles null optional fields`() {
        val dto = AnimeDataDto(
            malId = 1,
            title = "Test",
            type = null,
            images = null,
            synopsis = null,
            episodes = null,
            score = null,
            genres = null
        )

        val result = dto.toSearchResult()

        assertThat(result.imageUrl).isNull()
        assertThat(result.synopsis).isNull()
        assertThat(result.episodeCount).isNull()
        assertThat(result.score).isNull()
        assertThat(result.type).isNull()
        assertThat(result.genres).isEmpty()
    }
}
