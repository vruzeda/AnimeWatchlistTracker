package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeImagesDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeRelationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.BroadcastDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.StreamingDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.GenreDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ImageUrlDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.RelatedEntryDto
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
            broadcast = BroadcastDto(
                string = "Saturdays at 18:00 (JST)",
                day = "Saturdays",
                time = "18:00",
                timezone = "Asia/Tokyo"
            ),
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
        assertThat(details.broadcastInfo).isEqualTo("Saturdays at 18:00 (JST)")
        assertThat(details.broadcastDay).isEqualTo("Saturdays")
        assertThat(details.broadcastTime).isEqualTo("18:00")
        assertThat(details.broadcastTimezone).isEqualTo("Asia/Tokyo")
        assertThat(details.streamingLinks).isEmpty()
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
        assertThat(details.broadcastInfo).isNull()
        assertThat(details.broadcastDay).isNull()
        assertThat(details.broadcastTime).isNull()
        assertThat(details.broadcastTimezone).isNull()
        assertThat(details.streamingLinks).isEmpty()
    }

    @Test
    fun `maps streaming links when present`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test",
            streaming = listOf(
                StreamingDto(name = "Crunchyroll", url = "https://crunchyroll.com/test"),
                StreamingDto(name = "Netflix", url = "https://netflix.com/title/123")
            ),
            relations = null
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.streamingLinks).hasSize(2)
        assertThat(details.streamingLinks[0].name).isEqualTo("Crunchyroll")
        assertThat(details.streamingLinks[0].url).isEqualTo("https://crunchyroll.com/test")
        assertThat(details.streamingLinks[1].name).isEqualTo("Netflix")
        assertThat(details.streamingLinks[1].url).isEqualTo("https://netflix.com/title/123")
    }

    @Test
    fun `returns empty streamingLinks when streaming is null`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test",
            streaming = null,
            relations = null
        )

        assertThat(dto.toAnimeFullDetails().streamingLinks).isEmpty()
    }

    @Test
    fun `maps broadcast string when present`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test",
            broadcast = BroadcastDto(string = "Sundays at 00:00 (JST)"),
            relations = null
        )

        assertThat(dto.toAnimeFullDetails().broadcastInfo).isEqualTo("Sundays at 00:00 (JST)")
    }

    @Test
    fun `maps structured broadcast fields when present`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test",
            broadcast = BroadcastDto(
                string = "Sundays at 00:00 (JST)",
                day = "Sundays",
                time = "00:00",
                timezone = "Asia/Tokyo"
            ),
            relations = null
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.broadcastDay).isEqualTo("Sundays")
        assertThat(details.broadcastTime).isEqualTo("00:00")
        assertThat(details.broadcastTimezone).isEqualTo("Asia/Tokyo")
    }

    @Test
    fun `returns null broadcastInfo when broadcast is null`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test",
            broadcast = null,
            relations = null
        )

        assertThat(dto.toAnimeFullDetails().broadcastInfo).isNull()
    }

    @Test
    fun `returns null broadcastInfo when broadcast string is null`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test",
            broadcast = BroadcastDto(string = null),
            relations = null
        )

        assertThat(dto.toAnimeFullDetails().broadcastInfo).isNull()
    }
}
