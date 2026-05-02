package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class HomeSortStateTest {

    @Test
    fun `default state has ALPHABETICAL option and ascending true`() {
        val state = HomeSortState()

        assertThat(state.option).isEqualTo(HomeSortOption.ALPHABETICAL)
        assertThat(state.isAscending).isTrue()
    }

    @Test
    fun `custom state holds provided option and direction`() {
        val state = HomeSortState(
            option = HomeSortOption.USER_RATING,
            isAscending = false
        )

        assertThat(state.option).isEqualTo(HomeSortOption.USER_RATING)
        assertThat(state.isAscending).isFalse()
    }

    @Test
    fun `two states with same values are equal`() {
        val a = HomeSortState(option = HomeSortOption.RECENTLY_ADDED, isAscending = false)
        val b = HomeSortState(option = HomeSortOption.RECENTLY_ADDED, isAscending = false)

        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `states with different options are not equal`() {
        val a = HomeSortState(option = HomeSortOption.ALPHABETICAL)
        val b = HomeSortState(option = HomeSortOption.WATCH_STATUS)

        assertThat(a).isNotEqualTo(b)
    }
}
