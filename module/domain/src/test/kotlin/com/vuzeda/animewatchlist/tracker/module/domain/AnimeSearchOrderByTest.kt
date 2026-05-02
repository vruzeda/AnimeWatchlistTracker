package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeSearchOrderByTest {

    @Test
    fun `DEFAULT has null apiValue and defaultAscending true`() {
        assertThat(AnimeSearchOrderBy.DEFAULT.apiValue).isNull()
        assertThat(AnimeSearchOrderBy.DEFAULT.defaultAscending).isTrue()
    }

    @Test
    fun `SCORE has apiValue score and defaultAscending false`() {
        assertThat(AnimeSearchOrderBy.SCORE.apiValue).isEqualTo("score")
        assertThat(AnimeSearchOrderBy.SCORE.defaultAscending).isFalse()
    }

    @Test
    fun `RANK has apiValue rank and defaultAscending true`() {
        assertThat(AnimeSearchOrderBy.RANK.apiValue).isEqualTo("rank")
        assertThat(AnimeSearchOrderBy.RANK.defaultAscending).isTrue()
    }

    @Test
    fun `POPULARITY has apiValue popularity and defaultAscending true`() {
        assertThat(AnimeSearchOrderBy.POPULARITY.apiValue).isEqualTo("popularity")
        assertThat(AnimeSearchOrderBy.POPULARITY.defaultAscending).isTrue()
    }

    @Test
    fun `MEMBERS has apiValue members and defaultAscending false`() {
        assertThat(AnimeSearchOrderBy.MEMBERS.apiValue).isEqualTo("members")
        assertThat(AnimeSearchOrderBy.MEMBERS.defaultAscending).isFalse()
    }

    @Test
    fun `FAVORITES has apiValue favorites and defaultAscending false`() {
        assertThat(AnimeSearchOrderBy.FAVORITES.apiValue).isEqualTo("favorites")
        assertThat(AnimeSearchOrderBy.FAVORITES.defaultAscending).isFalse()
    }

    @Test
    fun `START_DATE has apiValue start_date and defaultAscending false`() {
        assertThat(AnimeSearchOrderBy.START_DATE.apiValue).isEqualTo("start_date")
        assertThat(AnimeSearchOrderBy.START_DATE.defaultAscending).isFalse()
    }

    @Test
    fun `TITLE has apiValue title and defaultAscending true`() {
        assertThat(AnimeSearchOrderBy.TITLE.apiValue).isEqualTo("title")
        assertThat(AnimeSearchOrderBy.TITLE.defaultAscending).isTrue()
    }

    @Test
    fun `values contains all eight options`() {
        assertThat(AnimeSearchOrderBy.entries).containsExactly(
            AnimeSearchOrderBy.DEFAULT,
            AnimeSearchOrderBy.SCORE,
            AnimeSearchOrderBy.RANK,
            AnimeSearchOrderBy.POPULARITY,
            AnimeSearchOrderBy.MEMBERS,
            AnimeSearchOrderBy.FAVORITES,
            AnimeSearchOrderBy.START_DATE,
            AnimeSearchOrderBy.TITLE
        )
    }
}
