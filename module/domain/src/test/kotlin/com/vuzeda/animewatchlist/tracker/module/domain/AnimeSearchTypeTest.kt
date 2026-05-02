package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeSearchTypeTest {

    @Test
    fun `ALL has null apiValue`() {
        assertThat(AnimeSearchType.ALL.apiValue).isNull()
    }

    @Test
    fun `TV has apiValue tv`() {
        assertThat(AnimeSearchType.TV.apiValue).isEqualTo("tv")
    }

    @Test
    fun `MOVIE has apiValue movie`() {
        assertThat(AnimeSearchType.MOVIE.apiValue).isEqualTo("movie")
    }

    @Test
    fun `OVA has apiValue ova`() {
        assertThat(AnimeSearchType.OVA.apiValue).isEqualTo("ova")
    }

    @Test
    fun `SPECIAL has apiValue special`() {
        assertThat(AnimeSearchType.SPECIAL.apiValue).isEqualTo("special")
    }

    @Test
    fun `ONA has apiValue ona`() {
        assertThat(AnimeSearchType.ONA.apiValue).isEqualTo("ona")
    }

    @Test
    fun `MUSIC has apiValue music`() {
        assertThat(AnimeSearchType.MUSIC.apiValue).isEqualTo("music")
    }

    @Test
    fun `values contains all seven types`() {
        assertThat(AnimeSearchType.entries).containsExactly(
            AnimeSearchType.ALL,
            AnimeSearchType.TV,
            AnimeSearchType.MOVIE,
            AnimeSearchType.OVA,
            AnimeSearchType.SPECIAL,
            AnimeSearchType.ONA,
            AnimeSearchType.MUSIC
        )
    }
}
