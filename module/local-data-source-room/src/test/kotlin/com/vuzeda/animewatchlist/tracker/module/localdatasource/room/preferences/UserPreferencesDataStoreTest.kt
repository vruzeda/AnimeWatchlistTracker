package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.preferences

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.IOException

class UserPreferencesDataStoreTest {

    @Test
    fun `flow catch emits default on error`() = runTest {
        val result = flow<String> { throw IOException() }
            .map { "original" }
            .catch { emit("default") }
            .first()

        assertThat(result).isEqualTo("default")
    }

    @Test
    fun `flow catch preserves value on success`() = runTest {
        val result = flow { emit("value") }
            .map { "processed" }
            .catch { emit("default") }
            .first()

        assertThat(result).isEqualTo("processed")
    }
}
