package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class WatchStatusTest {

    @Test
    fun `values contains all five statuses`() {
        assertThat(WatchStatus.entries).containsExactly(
            WatchStatus.WATCHING,
            WatchStatus.COMPLETED,
            WatchStatus.PLAN_TO_WATCH,
            WatchStatus.ON_HOLD,
            WatchStatus.DROPPED
        )
    }

    @Test
    fun `WATCHING name is WATCHING`() {
        assertThat(WatchStatus.WATCHING.name).isEqualTo("WATCHING")
    }

    @Test
    fun `COMPLETED name is COMPLETED`() {
        assertThat(WatchStatus.COMPLETED.name).isEqualTo("COMPLETED")
    }

    @Test
    fun `PLAN_TO_WATCH name is PLAN_TO_WATCH`() {
        assertThat(WatchStatus.PLAN_TO_WATCH.name).isEqualTo("PLAN_TO_WATCH")
    }

    @Test
    fun `ON_HOLD name is ON_HOLD`() {
        assertThat(WatchStatus.ON_HOLD.name).isEqualTo("ON_HOLD")
    }

    @Test
    fun `DROPPED name is DROPPED`() {
        assertThat(WatchStatus.DROPPED.name).isEqualTo("DROPPED")
    }
}
