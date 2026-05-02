package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ResolvedSeriesTest {

    @Test
    fun `ResolvedSeries holds title and seasons`() {
        val season = SeasonData(malId = 1, title = "Season 1", type = "TV")
        val series = ResolvedSeries(title = "Attack on Titan", seasons = listOf(season))

        assertThat(series.title).isEqualTo("Attack on Titan")
        assertThat(series.seasons).containsExactly(season)
    }

    @Test
    fun `ResolvedSeries optional fields default to null or empty`() {
        val series = ResolvedSeries(title = "Test", seasons = emptyList())

        assertThat(series.titleEnglish).isNull()
        assertThat(series.titleJapanese).isNull()
        assertThat(series.imageUrl).isNull()
        assertThat(series.synopsis).isNull()
        assertThat(series.genres).isEmpty()
    }

    @Test
    fun `ResolvedSeries with all optional fields populated`() {
        val series = ResolvedSeries(
            title = "Attack on Titan",
            titleEnglish = "Attack on Titan",
            titleJapanese = "進撃の巨人",
            imageUrl = "https://example.com/aot.jpg",
            synopsis = "Humanity fights for survival.",
            genres = listOf("Action", "Drama"),
            seasons = emptyList()
        )

        assertThat(series.titleEnglish).isEqualTo("Attack on Titan")
        assertThat(series.titleJapanese).isEqualTo("進撃の巨人")
        assertThat(series.genres).containsExactly("Action", "Drama")
    }

    @Test
    fun `two ResolvedSeries with same values are equal`() {
        val a = ResolvedSeries(title = "Test", seasons = emptyList())
        val b = ResolvedSeries(title = "Test", seasons = emptyList())

        assertThat(a).isEqualTo(b)
    }
}
