package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeImagesDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeRelationDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.GenreDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.ImageUrlDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.RelatedEntryDto
import org.junit.jupiter.api.Test

class AnimeFullDtoMapperTest {

    @Test
    fun `maps all fields correctly`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Attack on Titan",
            type = "TV",
            images = AnimeImagesDto(
                jpg = ImageUrlDto(
                    largeImageUrl = "https://example.com/aot.jpg",
                    imageUrl = "https://example.com/aot_small.jpg"
                )
            ),
            episodes = 24,
            score = 8.5,
            synopsis = "Humanity fights titans.",
            genres = listOf(GenreDto(name = "Action"), GenreDto(name = "Drama")),
            status = "Finished Airing",
            relations = null
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.malId).isEqualTo(100)
        assertThat(details.title).isEqualTo("Attack on Titan")
        assertThat(details.imageUrl).isEqualTo("https://example.com/aot.jpg")
        assertThat(details.type).isEqualTo("TV")
        assertThat(details.episodes).isEqualTo(24)
        assertThat(details.score).isEqualTo(8.5)
        assertThat(details.synopsis).isEqualTo("Humanity fights titans.")
        assertThat(details.genres).containsExactly("Action", "Drama")
        assertThat(details.airingStatus).isEqualTo("Finished Airing")
    }

    @Test
    fun `defaults type to Unknown when null`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test",
            type = null,
            relations = null
        )

        assertThat(dto.toAnimeFullDetails().type).isEqualTo("Unknown")
    }

    @Test
    fun `extracts only anime sequels from relations`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            relations = listOf(
                AnimeRelationDto(
                    relation = "Sequel",
                    entry = listOf(
                        RelatedEntryDto(malId = 200, type = "anime", name = "Test Season 2"),
                        RelatedEntryDto(malId = 300, type = "manga", name = "Test Manga")
                    )
                )
            )
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.sequels).hasSize(1)
        assertThat(details.sequels[0].malId).isEqualTo(200)
        assertThat(details.sequels[0].title).isEqualTo("Test Season 2")
    }

    @Test
    fun `returns empty sequels when relations is null`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            relations = null
        )

        assertThat(dto.toAnimeFullDetails().sequels).isEmpty()
    }

    @Test
    fun `extracts prequels from relations`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            relations = listOf(
                AnimeRelationDto(
                    relation = "Prequel",
                    entry = listOf(
                        RelatedEntryDto(malId = 50, type = "anime", name = "Test Prequel")
                    )
                ),
                AnimeRelationDto(
                    relation = "Sequel",
                    entry = listOf(
                        RelatedEntryDto(malId = 200, type = "anime", name = "Test Sequel")
                    )
                )
            )
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.prequels).hasSize(1)
        assertThat(details.prequels[0].malId).isEqualTo(50)
        assertThat(details.sequels).hasSize(1)
        assertThat(details.sequels[0].malId).isEqualTo(200)
    }

    @Test
    fun `filters non-anime entries from prequels`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            relations = listOf(
                AnimeRelationDto(
                    relation = "Prequel",
                    entry = listOf(
                        RelatedEntryDto(malId = 50, type = "anime", name = "Anime Prequel"),
                        RelatedEntryDto(malId = 60, type = "manga", name = "Manga Prequel")
                    )
                )
            )
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.prequels).hasSize(1)
        assertThat(details.prequels[0].malId).isEqualTo(50)
    }

    @Test
    fun `handles null optional fields`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test",
            images = null,
            episodes = null,
            score = null,
            synopsis = null,
            genres = null,
            status = null,
            relations = null
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.imageUrl).isNull()
        assertThat(details.episodes).isNull()
        assertThat(details.score).isNull()
        assertThat(details.synopsis).isNull()
        assertThat(details.genres).isEmpty()
        assertThat(details.airingStatus).isNull()
    }
}
