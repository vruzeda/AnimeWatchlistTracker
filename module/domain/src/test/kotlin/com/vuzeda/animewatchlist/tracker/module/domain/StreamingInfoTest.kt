package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class StreamingInfoTest {

    @Test
    fun `StreamingInfo holds name and url`() {
        val info = StreamingInfo(name = "Crunchyroll", url = "https://crunchyroll.com")

        assertThat(info.name).isEqualTo("Crunchyroll")
        assertThat(info.url).isEqualTo("https://crunchyroll.com")
    }

    @Test
    fun `two StreamingInfo with same values are equal`() {
        val a = StreamingInfo(name = "Netflix", url = "https://netflix.com")
        val b = StreamingInfo(name = "Netflix", url = "https://netflix.com")

        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `StreamingInfo with different url are not equal`() {
        val a = StreamingInfo(name = "Netflix", url = "https://netflix.com/anime-a")
        val b = StreamingInfo(name = "Netflix", url = "https://netflix.com/anime-b")

        assertThat(a).isNotEqualTo(b)
    }
}
