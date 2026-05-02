package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SearchFilterStateTest {

    @Test
    fun `default state has ALL type, ALL status, DEFAULT order, and ascending true`() {
        val state = SearchFilterState()

        assertThat(state.type).isEqualTo(AnimeSearchType.ALL)
        assertThat(state.status).isEqualTo(AnimeSearchStatus.ALL)
        assertThat(state.orderBy).isEqualTo(AnimeSearchOrderBy.DEFAULT)
        assertThat(state.isAscending).isTrue()
    }

    @Test
    fun `custom state holds all provided field values`() {
        val state = SearchFilterState(
            type = AnimeSearchType.TV,
            status = AnimeSearchStatus.AIRING,
            orderBy = AnimeSearchOrderBy.SCORE,
            isAscending = false
        )

        assertThat(state.type).isEqualTo(AnimeSearchType.TV)
        assertThat(state.status).isEqualTo(AnimeSearchStatus.AIRING)
        assertThat(state.orderBy).isEqualTo(AnimeSearchOrderBy.SCORE)
        assertThat(state.isAscending).isFalse()
    }

    @Test
    fun `two states with same values are equal`() {
        val a = SearchFilterState(type = AnimeSearchType.MOVIE, status = AnimeSearchStatus.COMPLETE)
        val b = SearchFilterState(type = AnimeSearchType.MOVIE, status = AnimeSearchStatus.COMPLETE)

        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `states with different type are not equal`() {
        val a = SearchFilterState(type = AnimeSearchType.TV)
        val b = SearchFilterState(type = AnimeSearchType.MOVIE)

        assertThat(a).isNotEqualTo(b)
    }
}
