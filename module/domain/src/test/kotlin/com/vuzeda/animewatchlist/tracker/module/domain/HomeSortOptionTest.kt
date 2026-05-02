package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class HomeSortOptionTest {

    @Test
    fun `ALPHABETICAL has defaultAscending true`() {
        assertThat(HomeSortOption.ALPHABETICAL.defaultAscending).isTrue()
    }

    @Test
    fun `RECENTLY_ADDED has defaultAscending false`() {
        assertThat(HomeSortOption.RECENTLY_ADDED.defaultAscending).isFalse()
    }

    @Test
    fun `USER_RATING has defaultAscending false`() {
        assertThat(HomeSortOption.USER_RATING.defaultAscending).isFalse()
    }

    @Test
    fun `WATCH_STATUS has defaultAscending true`() {
        assertThat(HomeSortOption.WATCH_STATUS.defaultAscending).isTrue()
    }

    @Test
    fun `values contains all four options`() {
        assertThat(HomeSortOption.entries).containsExactly(
            HomeSortOption.ALPHABETICAL,
            HomeSortOption.RECENTLY_ADDED,
            HomeSortOption.USER_RATING,
            HomeSortOption.WATCH_STATUS
        )
    }
}
