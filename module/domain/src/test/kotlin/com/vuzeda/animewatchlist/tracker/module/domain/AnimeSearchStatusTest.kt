package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeSearchStatusTest {

    @Test
    fun `entries contains all four statuses`() {
        assertThat(AnimeSearchStatus.entries).containsExactly(
            AnimeSearchStatus.ALL,
            AnimeSearchStatus.AIRING,
            AnimeSearchStatus.COMPLETE,
            AnimeSearchStatus.UPCOMING
        )
    }
}
