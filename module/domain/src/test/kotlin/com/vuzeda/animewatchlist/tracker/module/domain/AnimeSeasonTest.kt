package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeSeasonTest {

    @Test
    fun `next returns SPRING and year offset 0 for WINTER`() {
        val (season, offset) = AnimeSeason.WINTER.next()

        assertThat(season).isEqualTo(AnimeSeason.SPRING)
        assertThat(offset).isEqualTo(0)
    }

    @Test
    fun `next returns SUMMER and year offset 0 for SPRING`() {
        val (season, offset) = AnimeSeason.SPRING.next()

        assertThat(season).isEqualTo(AnimeSeason.SUMMER)
        assertThat(offset).isEqualTo(0)
    }

    @Test
    fun `next returns FALL and year offset 0 for SUMMER`() {
        val (season, offset) = AnimeSeason.SUMMER.next()

        assertThat(season).isEqualTo(AnimeSeason.FALL)
        assertThat(offset).isEqualTo(0)
    }

    @Test
    fun `next returns WINTER and year offset 1 for FALL`() {
        val (season, offset) = AnimeSeason.FALL.next()

        assertThat(season).isEqualTo(AnimeSeason.WINTER)
        assertThat(offset).isEqualTo(1)
    }

    @Test
    fun `previous returns FALL and year offset minus 1 for WINTER`() {
        val (season, offset) = AnimeSeason.WINTER.previous()

        assertThat(season).isEqualTo(AnimeSeason.FALL)
        assertThat(offset).isEqualTo(-1)
    }

    @Test
    fun `previous returns WINTER and year offset 0 for SPRING`() {
        val (season, offset) = AnimeSeason.SPRING.previous()

        assertThat(season).isEqualTo(AnimeSeason.WINTER)
        assertThat(offset).isEqualTo(0)
    }

    @Test
    fun `previous returns SPRING and year offset 0 for SUMMER`() {
        val (season, offset) = AnimeSeason.SUMMER.previous()

        assertThat(season).isEqualTo(AnimeSeason.SPRING)
        assertThat(offset).isEqualTo(0)
    }

    @Test
    fun `previous returns SUMMER and year offset 0 for FALL`() {
        val (season, offset) = AnimeSeason.FALL.previous()

        assertThat(season).isEqualTo(AnimeSeason.SUMMER)
        assertThat(offset).isEqualTo(0)
    }

    @Test
    fun `apiValue returns lowercase season name`() {
        assertThat(AnimeSeason.WINTER.apiValue).isEqualTo("winter")
        assertThat(AnimeSeason.SPRING.apiValue).isEqualTo("spring")
        assertThat(AnimeSeason.SUMMER.apiValue).isEqualTo("summer")
        assertThat(AnimeSeason.FALL.apiValue).isEqualTo("fall")
    }
}
