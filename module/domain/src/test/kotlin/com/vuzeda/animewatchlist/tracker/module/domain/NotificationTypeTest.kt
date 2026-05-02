package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class NotificationTypeTest {

    @Test
    fun `values contains all four types`() {
        assertThat(NotificationType.entries).containsExactly(
            NotificationType.NONE,
            NotificationType.NEW_EPISODES,
            NotificationType.NEW_SEASONS,
            NotificationType.BOTH
        )
    }

    @Test
    fun `NONE ordinal is 0`() {
        assertThat(NotificationType.NONE.ordinal).isEqualTo(0)
    }

    @Test
    fun `NEW_EPISODES ordinal is 1`() {
        assertThat(NotificationType.NEW_EPISODES.ordinal).isEqualTo(1)
    }

    @Test
    fun `NEW_SEASONS ordinal is 2`() {
        assertThat(NotificationType.NEW_SEASONS.ordinal).isEqualTo(2)
    }

    @Test
    fun `BOTH ordinal is 3`() {
        assertThat(NotificationType.BOTH.ordinal).isEqualTo(3)
    }
}
