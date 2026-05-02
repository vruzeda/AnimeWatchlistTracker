package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class EpisodeInfoTest {

    @Test
    fun `EpisodeInfo holds number, title, aired, and flags`() {
        val episode = EpisodeInfo(
            number = 1,
            title = "Pilot",
            aired = "2003-10-04",
            isFiller = false,
            isRecap = false
        )

        assertThat(episode.number).isEqualTo(1)
        assertThat(episode.title).isEqualTo("Pilot")
        assertThat(episode.aired).isEqualTo("2003-10-04")
        assertThat(episode.isFiller).isFalse()
        assertThat(episode.isRecap).isFalse()
    }

    @Test
    fun `isPlaceholder defaults to false`() {
        val episode = EpisodeInfo(number = 1, title = null, aired = null, isFiller = false, isRecap = false)

        assertThat(episode.isPlaceholder).isFalse()
    }

    @Test
    fun `isPlaceholder can be set to true`() {
        val episode = EpisodeInfo(
            number = 0,
            title = null,
            aired = null,
            isFiller = false,
            isRecap = false,
            isPlaceholder = true
        )

        assertThat(episode.isPlaceholder).isTrue()
    }

    @Test
    fun `EpisodeInfo with null title and aired`() {
        val episode = EpisodeInfo(number = 5, title = null, aired = null, isFiller = true, isRecap = false)

        assertThat(episode.title).isNull()
        assertThat(episode.aired).isNull()
        assertThat(episode.isFiller).isTrue()
    }

    @Test
    fun `two EpisodeInfo with same values are equal`() {
        val a = EpisodeInfo(number = 1, title = "Ep1", aired = "2024-01-01", isFiller = false, isRecap = false)
        val b = EpisodeInfo(number = 1, title = "Ep1", aired = "2024-01-01", isFiller = false, isRecap = false)

        assertThat(a).isEqualTo(b)
    }
}
