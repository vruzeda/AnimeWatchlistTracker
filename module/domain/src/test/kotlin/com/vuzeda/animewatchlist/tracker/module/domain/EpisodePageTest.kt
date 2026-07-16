package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class EpisodePageTest {

    @Test
    fun `EpisodePage holds episodes, hasNextPage, and nextPage`() {
        val episodes = listOf(
            EpisodeInfo(
                number = 1,
                titleRomaji = "Ep1",
                titleEnglish = "Ep1",
                titleJapanese = "Ep1",
                aired = null,
                isFiller = false,
                isRecap = false
            )
        )
        val page = EpisodePage(episodes = episodes, hasNextPage = true, nextPage = 2)

        assertThat(page.episodes).isEqualTo(episodes)
        assertThat(page.hasNextPage).isTrue()
        assertThat(page.nextPage).isEqualTo(2)
    }

    @Test
    fun `EpisodePage with empty episodes and no next page`() {
        val page = EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 1)

        assertThat(page.episodes).isEmpty()
        assertThat(page.hasNextPage).isFalse()
        assertThat(page.nextPage).isEqualTo(1)
    }

    @Test
    fun `two EpisodePage with same values are equal`() {
        val a = EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 1)
        val b = EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 1)

        assertThat(a).isEqualTo(b)
    }
}
