package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SeasonDataTest {

    @Test
    fun `SeasonData holds malId, title, and type`() {
        val data = SeasonData(malId = 5114, title = "FMA Brotherhood", type = "TV")

        assertThat(data.malId).isEqualTo(5114)
        assertThat(data.title).isEqualTo("FMA Brotherhood")
        assertThat(data.type).isEqualTo("TV")
    }

    @Test
    fun `SeasonData optional fields default to null, empty, or true`() {
        val data = SeasonData(malId = 1, title = "Test", type = "TV")

        assertThat(data.titleEnglish).isNull()
        assertThat(data.titleJapanese).isNull()
        assertThat(data.imageUrl).isNull()
        assertThat(data.episodeCount).isNull()
        assertThat(data.score).isNull()
        assertThat(data.airingStatus).isNull()
        assertThat(data.synopsis).isNull()
        assertThat(data.genres).isEmpty()
        assertThat(data.isMainSeries).isTrue()
        assertThat(data.startDate).isNull()
    }

    @Test
    fun `SeasonData with all fields populated`() {
        val startDate = LocalDate.of(2009, 4, 5)
        val data = SeasonData(
            malId = 5114,
            title = "FMA Brotherhood",
            titleEnglish = "Fullmetal Alchemist: Brotherhood",
            titleJapanese = "鋼の錬金術師",
            imageUrl = "https://example.com/fma.jpg",
            type = "TV",
            episodeCount = 64,
            score = 9.1,
            airingStatus = "Finished Airing",
            synopsis = "Two brothers seek the Philosopher's Stone.",
            genres = listOf("Action", "Adventure"),
            isMainSeries = true,
            startDate = startDate
        )

        assertThat(data.episodeCount).isEqualTo(64)
        assertThat(data.score).isEqualTo(9.1)
        assertThat(data.isMainSeries).isTrue()
        assertThat(data.startDate).isEqualTo(startDate)
    }

    @Test
    fun `SeasonData with isMainSeries false`() {
        val data = SeasonData(malId = 1, title = "OVA Spinoff", type = "OVA", isMainSeries = false)

        assertThat(data.isMainSeries).isFalse()
    }

    @Test
    fun `two SeasonData with same values are equal`() {
        val a = SeasonData(malId = 1, title = "Test", type = "TV")
        val b = SeasonData(malId = 1, title = "Test", type = "TV")

        assertThat(a).isEqualTo(b)
    }
}
