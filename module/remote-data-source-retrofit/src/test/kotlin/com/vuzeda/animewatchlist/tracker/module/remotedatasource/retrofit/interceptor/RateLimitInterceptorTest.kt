package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.interceptor

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import org.junit.jupiter.api.Test

class RateLimitInterceptorTest {

    private fun buildChain(host: String): Interceptor.Chain {
        val request = Request.Builder()
            .url("https://$host/")
            .build()
        return mockk {
            every { request() } returns request
            every { proceed(any()) } returns mockk(relaxed = true)
        }
    }

    @Test
    fun `non-Jikan host passes through without rate limiting`() {
        val interceptor = RateLimitInterceptor(minIntervalMs = 10_000)
        val chain = buildChain("example.com")

        interceptor.intercept(chain)

        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun `Jikan host passes through when interval is zero`() {
        val interceptor = RateLimitInterceptor(minIntervalMs = 0)
        val chain = buildChain(RateLimitInterceptor.JIKAN_HOST)

        interceptor.intercept(chain)
        interceptor.intercept(chain)

        verify(exactly = 2) { chain.proceed(any()) }
    }

    @Test
    fun `Jikan host delays second request when called too soon`() {
        val interceptor = RateLimitInterceptor(minIntervalMs = 100)
        val chain = buildChain(RateLimitInterceptor.JIKAN_HOST)

        interceptor.intercept(chain)
        val startMs = System.currentTimeMillis()
        interceptor.intercept(chain)
        val elapsedMs = System.currentTimeMillis() - startMs

        assertThat(elapsedMs).isAtLeast(50L)
        verify(exactly = 2) { chain.proceed(any()) }
    }

    @Test
    fun `default interval constant is 334ms`() {
        assertThat(RateLimitInterceptor.DEFAULT_INTERVAL_MS).isEqualTo(334L)
    }

    @Test
    fun `Jikan host constant matches api subdomain`() {
        assertThat(RateLimitInterceptor.JIKAN_HOST).isEqualTo("api.jikan.moe")
    }
}
