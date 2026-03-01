package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeRelationDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.RelatedEntryDto
import org.junit.jupiter.api.Test

class AnimeFullDtoMapperTest {

    @Test
    fun `maps malId and episodes correctly`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            episodes = 24,
            relations = null
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.malId).isEqualTo(100)
        assertThat(details.episodes).isEqualTo(24)
    }

    @Test
    fun `extracts only anime sequels from relations`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            episodes = 12,
            relations = listOf(
                AnimeRelationDto(
                    relation = "Sequel",
                    entry = listOf(
                        RelatedEntryDto(malId = 200, type = "anime", name = "Test Season 2"),
                        RelatedEntryDto(malId = 300, type = "manga", name = "Test Manga")
                    )
                ),
                AnimeRelationDto(
                    relation = "Prequel",
                    entry = listOf(
                        RelatedEntryDto(malId = 50, type = "anime", name = "Test Prequel")
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
            episodes = 12,
            relations = null
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.sequels).isEmpty()
    }

    @Test
    fun `returns empty sequels when no sequel relations exist`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            episodes = 12,
            relations = listOf(
                AnimeRelationDto(
                    relation = "Prequel",
                    entry = listOf(
                        RelatedEntryDto(malId = 50, type = "anime", name = "Prequel")
                    )
                )
            )
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.sequels).isEmpty()
    }

    @Test
    fun `handles multiple sequel entries`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            episodes = 12,
            relations = listOf(
                AnimeRelationDto(
                    relation = "Sequel",
                    entry = listOf(
                        RelatedEntryDto(malId = 200, type = "anime", name = "Season 2"),
                        RelatedEntryDto(malId = 300, type = "anime", name = "Season 3")
                    )
                )
            )
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.sequels).hasSize(2)
        assertThat(details.sequels.map { it.malId }).containsExactly(200, 300)
    }

    @Test
    fun `handles null episodes`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            episodes = null,
            relations = null
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.episodes).isNull()
    }

    @Test
    fun `extracts prequels from relations`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            episodes = 12,
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
        assertThat(details.prequels[0].title).isEqualTo("Test Prequel")
        assertThat(details.sequels).hasSize(1)
        assertThat(details.sequels[0].malId).isEqualTo(200)
    }

    @Test
    fun `returns empty prequels when none exist`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            episodes = 12,
            relations = listOf(
                AnimeRelationDto(
                    relation = "Sequel",
                    entry = listOf(
                        RelatedEntryDto(malId = 200, type = "anime", name = "Sequel")
                    )
                )
            )
        )

        val details = dto.toAnimeFullDetails()

        assertThat(details.prequels).isEmpty()
    }

    @Test
    fun `filters non-anime entries from prequels`() {
        val dto = AnimeFullDataDto(
            malId = 100,
            title = "Test Anime",
            episodes = 12,
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
}
