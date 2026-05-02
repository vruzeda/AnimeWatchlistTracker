package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SearchResultTest {

    @Test
    fun `SearchResult holds malId and title`() {
        val result = SearchResult(malId = 1, title = "Naruto")

        assertThat(result.malId).isEqualTo(1)
        assertThat(result.title).isEqualTo("Naruto")
    }

    @Test
    fun `SearchResult optional fields default to null or empty`() {
        val result = SearchResult(malId = 1, title = "Naruto")

        assertThat(result.titleEnglish).isNull()
        assertThat(result.titleJapanese).isNull()
        assertThat(result.imageUrl).isNull()
        assertThat(result.synopsis).isNull()
        assertThat(result.episodeCount).isNull()
        assertThat(result.score).isNull()
        assertThat(result.type).isNull()
        assertThat(result.genres).isEmpty()
    }

    @Test
    fun `SearchResult with all fields populated`() {
        val result = SearchResult(
            malId = 20,
            title = "Naruto",
            titleEnglish = "Naruto",
            titleJapanese = "ナルト",
            imageUrl = "https://example.com/naruto.jpg",
            synopsis = "A young ninja seeks recognition.",
            episodeCount = 220,
            score = 7.9,
            type = "TV",
            genres = listOf("Action", "Adventure")
        )

        assertThat(result.titleEnglish).isEqualTo("Naruto")
        assertThat(result.episodeCount).isEqualTo(220)
        assertThat(result.score).isEqualTo(7.9)
        assertThat(result.genres).containsExactly("Action", "Adventure")
    }

    @Test
    fun `two SearchResult with same values are equal`() {
        val a = SearchResult(malId = 1, title = "Test")
        val b = SearchResult(malId = 1, title = "Test")

        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `SearchResult with different malId are not equal`() {
        val a = SearchResult(malId = 1, title = "Test")
        val b = SearchResult(malId = 2, title = "Test")

        assertThat(a).isNotEqualTo(b)
    }
}
