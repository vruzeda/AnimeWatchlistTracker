package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.api.dto.ChiakiWatchOrderEntryDto
import org.junit.jupiter.api.Test

class ChiakiDtoMapperTest {

    @Test
    fun `filters to only TV and Movie types`() {
        val entries = listOf(
            dto(malId = 1, typeCode = 1, title = "TV Show"),
            dto(malId = 2, typeCode = 2, title = "OVA"),
            dto(malId = 3, typeCode = 3, title = "Movie"),
            dto(malId = 4, typeCode = 4, title = "Special"),
            dto(malId = 5, typeCode = 5, title = "ONA"),
            dto(malId = 6, typeCode = 6, title = "Music")
        )

        val result = entries.toSeasonDataList()

        assertThat(result).hasSize(2)
        assertThat(result[0].malId).isEqualTo(1)
        assertThat(result[1].malId).isEqualTo(3)
    }

    @Test
    fun `maps type code 1 to TV`() {
        val entries = listOf(dto(malId = 1, typeCode = 1, title = "Test"))

        val result = entries.toSeasonDataList()

        assertThat(result[0].type).isEqualTo("TV")
    }

    @Test
    fun `maps type code 3 to Movie`() {
        val entries = listOf(dto(malId = 1, typeCode = 3, title = "Test"))

        val result = entries.toSeasonDataList()

        assertThat(result[0].type).isEqualTo("Movie")
    }

    @Test
    fun `maps all fields correctly`() {
        val entry = ChiakiWatchOrderEntryDto(
            malId = 16498,
            title = "Shingeki no Kyojin",
            typeCode = 1,
            episodeCount = 25,
            score = 8.54,
            imageUrl = "https://chiaki.site/media/images/16498.jpg"
        )

        val result = entry.toSeasonData()

        assertThat(result.malId).isEqualTo(16498)
        assertThat(result.title).isEqualTo("Shingeki no Kyojin")
        assertThat(result.type).isEqualTo("TV")
        assertThat(result.episodeCount).isEqualTo(25)
        assertThat(result.score).isEqualTo(8.54)
        assertThat(result.imageUrl).isEqualTo("https://chiaki.site/media/images/16498.jpg")
    }

    @Test
    fun `handles null optional fields`() {
        val entry = ChiakiWatchOrderEntryDto(
            malId = 100,
            title = "Minimal Entry",
            typeCode = 1,
            episodeCount = null,
            score = null,
            imageUrl = null
        )

        val result = entry.toSeasonData()

        assertThat(result.episodeCount).isNull()
        assertThat(result.score).isNull()
        assertThat(result.imageUrl).isNull()
    }

    @Test
    fun `returns empty list when no entries match allowed types`() {
        val entries = listOf(
            dto(malId = 1, typeCode = 2, title = "OVA"),
            dto(malId = 2, typeCode = 4, title = "Special")
        )

        val result = entries.toSeasonDataList()

        assertThat(result).isEmpty()
    }

    @Test
    fun `preserves order of entries`() {
        val entries = listOf(
            dto(malId = 10, typeCode = 1, title = "First"),
            dto(malId = 20, typeCode = 3, title = "Second"),
            dto(malId = 30, typeCode = 1, title = "Third")
        )

        val result = entries.toSeasonDataList()

        assertThat(result).hasSize(3)
        assertThat(result[0].malId).isEqualTo(10)
        assertThat(result[1].malId).isEqualTo(20)
        assertThat(result[2].malId).isEqualTo(30)
    }

    private fun dto(
        malId: Int,
        typeCode: Int,
        title: String
    ) = ChiakiWatchOrderEntryDto(
        malId = malId,
        title = title,
        typeCode = typeCode,
        episodeCount = null,
        score = null,
        imageUrl = null
    )
}
