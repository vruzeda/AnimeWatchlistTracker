package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.interceptor

import com.google.common.truth.Truth.assertThat
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Request
import org.junit.jupiter.api.Test

class BrowserUserAgentInterceptorTest {

    private fun buildChain(request: Request, proceededRequest: CapturingSlot<Request>): Interceptor.Chain =
        mockk {
            every { request() } returns request
            every { proceed(capture(proceededRequest)) } returns mockk(relaxed = true)
        }

    @Test
    fun `sets the configured User-Agent on requests without one`() {
        val interceptor = BrowserUserAgentInterceptor(userAgent = "TestAgent/1.0")
        val proceededRequest = slot<Request>()
        val chain = buildChain(
            request = Request.Builder().url("https://api.jikan.moe/").build(),
            proceededRequest = proceededRequest
        )

        interceptor.intercept(chain)

        assertThat(proceededRequest.captured.header("User-Agent")).isEqualTo("TestAgent/1.0")
    }

    @Test
    fun `replaces a pre-existing User-Agent`() {
        val interceptor = BrowserUserAgentInterceptor(userAgent = "TestAgent/1.0")
        val proceededRequest = slot<Request>()
        val chain = buildChain(
            request = Request.Builder()
                .url("https://chiaki.site/")
                .header("User-Agent", "OldAgent/0.1")
                .build(),
            proceededRequest = proceededRequest
        )

        interceptor.intercept(chain)

        assertThat(proceededRequest.captured.headers("User-Agent")).containsExactly("TestAgent/1.0")
    }

    @Test
    fun `sanitize removes WebView markers from a raw WebView User-Agent`() {
        val rawWebViewUserAgent =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/UQ1A.240105.004; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/126.0.6478.134 Mobile Safari/537.36"

        val sanitized = BrowserUserAgentInterceptor.sanitizeWebViewUserAgent(rawWebViewUserAgent)

        assertThat(sanitized).isEqualTo(
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/UQ1A.240105.004) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.134 Mobile Safari/537.36"
        )
    }

    @Test
    fun `sanitize leaves a regular browser User-Agent unchanged`() {
        val browserUserAgent = BrowserUserAgentInterceptor.FALLBACK_BROWSER_USER_AGENT

        val sanitized = BrowserUserAgentInterceptor.sanitizeWebViewUserAgent(browserUserAgent)

        assertThat(sanitized).isEqualTo(browserUserAgent)
    }

    @Test
    fun `sanitize falls back to the default User-Agent for blank input`() {
        val sanitized = BrowserUserAgentInterceptor.sanitizeWebViewUserAgent("   ")

        assertThat(sanitized).isEqualTo(BrowserUserAgentInterceptor.FALLBACK_BROWSER_USER_AGENT)
    }
}
