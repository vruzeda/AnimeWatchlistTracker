package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SeasonalAnimePageTest {

    @Test
    fun `SeasonalAnimePage holds results, hasNextPage, and currentPage`() {
        val results = listOf(SearchResult(malId = 1, title = "Anime A"))
        val page = SeasonalAnimePage(results = results, hasNextPage = true, currentPage = 2)

        assertThat(page.results).isEqualTo(results)
        assertThat(page.hasNextPage).isTrue()
        assertThat(page.currentPage).isEqualTo(2)
    }

    @Test
    fun `SeasonalAnimePage with empty results and no next page`() {
        val page = SeasonalAnimePage(results = emptyList(), hasNextPage = false, currentPage = 1)

        assertThat(page.results).isEmpty()
        assertThat(page.hasNextPage).isFalse()
    }

    @Test
    fun `two SeasonalAnimePage with same values are equal`() {
        val a = SeasonalAnimePage(results = emptyList(), hasNextPage = false, currentPage = 1)
        val b = SeasonalAnimePage(results = emptyList(), hasNextPage = false, currentPage = 1)

        assertThat(a).isEqualTo(b)
    }
}
