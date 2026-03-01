package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.api.dto.ChiakiWatchOrderEntryDto
import org.junit.jupiter.api.Test

class ChiakiDtoMapperTest {

    @Test
    fun `includes all entry types without filtering`() {
        val entries = listOf(
            dto(malId = 1, typeCode = 1, title = "TV Show"),
            dto(malId = 2, typeCode = 2, title = "OVA"),
            dto(malId = 3, typeCode = 3, title = "Movie"),
            dto(malId = 4, typeCode = 4, title = "Special"),
            dto(malId = 5, typeCode = 5, title = "ONA"),
            dto(malId = 6, typeCode = 6, title = "Music"),
            dto(malId = 7, typeCode = 7, title = "CM"),
            dto(malId = 8, typeCode = 8, title = "PV"),
            dto(malId = 9, typeCode = 9, title = "TV Special")
        )

        val result = entries.toSeasonDataList()

        assertThat(result).hasSize(9)
        assertThat(result.map { it.malId }).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9).inOrder()
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
    fun `returns empty list when input is empty`() {
        val result = emptyList<ChiakiWatchOrderEntryDto>().toSeasonDataList()

        assertThat(result).isEmpty()
    }

    @Test
    fun `maps type code 2 to OVA`() {
        val result = listOf(dto(malId = 1, typeCode = 2, title = "Test")).toSeasonDataList()

        assertThat(result[0].type).isEqualTo("OVA")
    }

    @Test
    fun `maps type code 4 to Special`() {
        val result = listOf(dto(malId = 1, typeCode = 4, title = "Test")).toSeasonDataList()

        assertThat(result[0].type).isEqualTo("Special")
    }

    @Test
    fun `maps type code 5 to ONA`() {
        val result = listOf(dto(malId = 1, typeCode = 5, title = "Test")).toSeasonDataList()

        assertThat(result[0].type).isEqualTo("ONA")
    }

    @Test
    fun `maps type code 6 to Music`() {
        val result = listOf(dto(malId = 1, typeCode = 6, title = "Test")).toSeasonDataList()

        assertThat(result[0].type).isEqualTo("Music")
    }

    @Test
    fun `maps type code 7 to CM`() {
        val result = listOf(dto(malId = 1, typeCode = 7, title = "Test")).toSeasonDataList()

        assertThat(result[0].type).isEqualTo("CM")
    }

    @Test
    fun `maps type code 8 to PV`() {
        val result = listOf(dto(malId = 1, typeCode = 8, title = "Test")).toSeasonDataList()

        assertThat(result[0].type).isEqualTo("PV")
    }

    @Test
    fun `maps type code 9 to TV Special`() {
        val result = listOf(dto(malId = 1, typeCode = 9, title = "Test")).toSeasonDataList()

        assertThat(result[0].type).isEqualTo("TV Special")
    }

    @Test
    fun `falls back to TV for unknown type codes`() {
        val result = listOf(dto(malId = 1, typeCode = 99, title = "Test")).toSeasonDataList()

        assertThat(result[0].type).isEqualTo("TV")
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
