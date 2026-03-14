package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromBoolean converts true to 1`() {
        assertThat(converters.fromBoolean(true)).isEqualTo(1)
    }

    @Test
    fun `fromBoolean converts false to 0`() {
        assertThat(converters.fromBoolean(false)).isEqualTo(0)
    }

    @Test
    fun `toBoolean converts 0 to false`() {
        assertThat(converters.toBoolean(0)).isFalse()
    }

    @Test
    fun `toBoolean converts 1 to true`() {
        assertThat(converters.toBoolean(1)).isTrue()
    }

    @Test
    fun `toBoolean converts any non-zero value to true`() {
        assertThat(converters.toBoolean(-1)).isTrue()
        assertThat(converters.toBoolean(42)).isTrue()
    }
}
