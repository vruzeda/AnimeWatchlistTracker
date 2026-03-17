package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
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

    @Test
    fun `fromLocalDate converts LocalDate to ISO string`() {
        val date = LocalDate.of(2026, 3, 15)

        assertThat(converters.fromLocalDate(date)).isEqualTo("2026-03-15")
    }

    @Test
    fun `fromLocalDate converts null to null`() {
        assertThat(converters.fromLocalDate(null)).isNull()
    }

    @Test
    fun `toLocalDate parses ISO string to LocalDate`() {
        val result = converters.toLocalDate("2026-03-15")

        assertThat(result).isEqualTo(LocalDate.of(2026, 3, 15))
    }

    @Test
    fun `toLocalDate converts null to null`() {
        assertThat(converters.toLocalDate(null)).isNull()
    }
}
