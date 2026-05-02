package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class HomeViewModeTest {

    @Test
    fun `values contains ANIME and SEASON`() {
        assertThat(HomeViewMode.entries).containsExactly(
            HomeViewMode.ANIME,
            HomeViewMode.SEASON
        )
    }

    @Test
    fun `ANIME name is ANIME`() {
        assertThat(HomeViewMode.ANIME.name).isEqualTo("ANIME")
    }

    @Test
    fun `SEASON name is SEASON`() {
        assertThat(HomeViewMode.SEASON.name).isEqualTo("SEASON")
    }
}
