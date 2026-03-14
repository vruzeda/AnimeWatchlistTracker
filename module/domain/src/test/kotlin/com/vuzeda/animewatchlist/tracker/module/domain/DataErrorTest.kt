package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DataErrorTest {

    @Test
    fun `Network with non-null throwable uses throwable message`() {
        val cause = RuntimeException("Connection refused")
        val error = DataError.Network(throwable = cause)

        assertThat(error.message).isEqualTo("Connection refused")
        assertThat(error.cause).isEqualTo(cause)
    }

    @Test
    fun `Network with null throwable uses default message`() {
        val error = DataError.Network(throwable = null)

        assertThat(error.message).isEqualTo("Network error")
        assertThat(error.cause).isNull()
    }

    @Test
    fun `NotFound uses default message when none provided`() {
        val error = DataError.NotFound()

        assertThat(error.message).isEqualTo("Not found")
    }

    @Test
    fun `NotFound uses custom error message`() {
        val error = DataError.NotFound(errorMessage = "Anime with id 999 not found")

        assertThat(error.message).isEqualTo("Anime with id 999 not found")
    }

    @Test
    fun `RateLimited with null retryAfterMs has basic message`() {
        val error = DataError.RateLimited(retryAfterMs = null)

        assertThat(error.message).isEqualTo("Rate limited")
    }

    @Test
    fun `RateLimited with non-null retryAfterMs includes retry duration in message`() {
        val error = DataError.RateLimited(retryAfterMs = 500L)

        assertThat(error.message).isEqualTo("Rate limited, retry after 500ms")
    }

    @Test
    fun `Unknown uses wrapped throwable message`() {
        val cause = IllegalStateException("Something went wrong")
        val error = DataError.Unknown(throwable = cause)

        assertThat(error.message).isEqualTo("Something went wrong")
        assertThat(error.cause).isEqualTo(cause)
    }
}
