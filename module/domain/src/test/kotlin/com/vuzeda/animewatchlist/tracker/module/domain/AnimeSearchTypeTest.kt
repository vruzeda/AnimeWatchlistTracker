package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeSearchTypeTest {

    @Test
    fun `entries contains all seven types`() {
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
