package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeSearchStatusTest {

    @Test
    fun `ALL has null apiValue`() {
        assertThat(AnimeSearchStatus.ALL.apiValue).isNull()
    }

    @Test
    fun `AIRING has apiValue airing`() {
        assertThat(AnimeSearchStatus.AIRING.apiValue).isEqualTo("airing")
    }

    @Test
    fun `COMPLETE has apiValue complete`() {
        assertThat(AnimeSearchStatus.COMPLETE.apiValue).isEqualTo("complete")
    }

    @Test
    fun `UPCOMING has apiValue upcoming`() {
        assertThat(AnimeSearchStatus.UPCOMING.apiValue).isEqualTo("upcoming")
    }

    @Test
    fun `values contains all four statuses`() {
        assertThat(AnimeSearchStatus.entries).containsExactly(
            AnimeSearchStatus.ALL,
            AnimeSearchStatus.AIRING,
            AnimeSearchStatus.COMPLETE,
            AnimeSearchStatus.UPCOMING
        )
    }
}
