package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeSearchOrderByTest {

    @Test
    fun `DEFAULT has defaultAscending true`() {
        assertThat(AnimeSearchOrderBy.DEFAULT.defaultAscending).isTrue()
    }

    @Test
    fun `SCORE has defaultAscending false`() {
        assertThat(AnimeSearchOrderBy.SCORE.defaultAscending).isFalse()
    }

    @Test
    fun `RANK has defaultAscending true`() {
        assertThat(AnimeSearchOrderBy.RANK.defaultAscending).isTrue()
    }

    @Test
    fun `entries contains all eight options`() {
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
