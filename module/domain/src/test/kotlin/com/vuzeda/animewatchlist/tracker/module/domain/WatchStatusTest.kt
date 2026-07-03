package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class WatchStatusTest {

    @Test
    fun `all WatchStatus entries are defined`() {
        val entries = WatchStatus.entries
        assertThat(entries).isNotEmpty()
        assertThat(entries).contains(WatchStatus.WATCHING)
        assertThat(entries).contains(WatchStatus.COMPLETED)
        assertThat(entries).contains(WatchStatus.PLAN_TO_WATCH)
    }

    @Test
    fun `PLAN_TO_WATCH has name PLAN_TO_WATCH`() {
        assertThat(WatchStatus.PLAN_TO_WATCH.name).isEqualTo("PLAN_TO_WATCH")
    }
}
