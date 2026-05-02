package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SearchResultPageTest {

    @Test
    fun `SearchResultPage holds results, hasNextPage, and currentPage`() {
        val results = listOf(SearchResult(malId = 1, title = "Test"))
        val page = SearchResultPage(results = results, hasNextPage = true, currentPage = 1)

        assertThat(page.results).isEqualTo(results)
        assertThat(page.hasNextPage).isTrue()
        assertThat(page.currentPage).isEqualTo(1)
    }

    @Test
    fun `SearchResultPage with empty results and no next page`() {
        val page = SearchResultPage(results = emptyList(), hasNextPage = false, currentPage = 1)

        assertThat(page.results).isEmpty()
        assertThat(page.hasNextPage).isFalse()
    }

    @Test
    fun `two SearchResultPage with same values are equal`() {
        val a = SearchResultPage(results = emptyList(), hasNextPage = false, currentPage = 1)
        val b = SearchResultPage(results = emptyList(), hasNextPage = false, currentPage = 1)

        assertThat(a).isEqualTo(b)
    }
}
