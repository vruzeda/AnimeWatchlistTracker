package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class EpisodeInfoTest {

    @Test
    fun `EpisodeInfo holds number, titleRomaji, aired, and flags`() {
        val episode = EpisodeInfo(
            number = 1,
            titleRomaji = "Pilot",
            titleEnglish = "Pilot",
            titleJapanese = "パイロット",
            aired = "2003-10-04",
            isFiller = false,
            isRecap = false
        )

        assertThat(episode.number).isEqualTo(1)
        assertThat(episode.titleRomaji).isEqualTo("Pilot")
        assertThat(episode.titleEnglish).isEqualTo("Pilot")
        assertThat(episode.titleJapanese).isEqualTo("パイロット")
        assertThat(episode.aired).isEqualTo("2003-10-04")
        assertThat(episode.isFiller).isFalse()
        assertThat(episode.isRecap).isFalse()
    }

    @Test
    fun `isPlaceholder defaults to false`() {
        val episode = EpisodeInfo(
            number = 1,
            titleRomaji = null,
            titleEnglish = null,
            titleJapanese = null,
            aired = null,
            isFiller = false,
            isRecap = false
        )

        assertThat(episode.isPlaceholder).isFalse()
    }

    @Test
    fun `isPlaceholder can be set to true`() {
        val episode = EpisodeInfo(
            number = 0,
            titleRomaji = null,
            titleEnglish = null,
            titleJapanese = null,
            aired = null,
            isFiller = false,
            isRecap = false,
            isPlaceholder = true
        )

        assertThat(episode.isPlaceholder).isTrue()
    }

    @Test
    fun `EpisodeInfo with null titleRomaji and aired`() {
        val episode = EpisodeInfo(
            number = 5,
            titleRomaji = null,
            titleEnglish = null,
            titleJapanese = null,
            aired = null,
            isFiller = true,
            isRecap = false
        )

        assertThat(episode.titleRomaji).isNull()
        assertThat(episode.aired).isNull()
        assertThat(episode.isFiller).isTrue()
    }

    @Test
    fun `two EpisodeInfo with same values are equal`() {
        val a = EpisodeInfo(
            number = 1,
            titleRomaji = "Ep1",
            titleEnglish = "Ep1",
            titleJapanese = "Ep1",
            aired = "2024-01-01",
            isFiller = false,
            isRecap = false
        )
        val b = EpisodeInfo(
            number = 1,
            titleRomaji = "Ep1",
            titleEnglish = "Ep1",
            titleJapanese = "Ep1",
            aired = "2024-01-01",
            isFiller = false,
            isRecap = false
        )

        assertThat(a).isEqualTo(b)
    }
}
