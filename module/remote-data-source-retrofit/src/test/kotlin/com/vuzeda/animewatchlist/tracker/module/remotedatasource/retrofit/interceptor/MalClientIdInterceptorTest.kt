package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.interceptor

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Request
import org.junit.jupiter.api.Test

class MalClientIdInterceptorTest {

    @Test
    fun `adds client id header to the request`() {
        val interceptor = MalClientIdInterceptor(clientId = "test-client-id")
        val requestSlot = slot<Request>()
        val chain: Interceptor.Chain = mockk {
            every { request() } returns Request.Builder().url("https://api.myanimelist.net/v2/anime").build()
            every { proceed(capture(requestSlot)) } returns mockk(relaxed = true)
        }

        interceptor.intercept(chain)

        assertThat(requestSlot.captured.header(MalClientIdInterceptor.CLIENT_ID_HEADER))
            .isEqualTo("test-client-id")
    }

    @Test
    fun `replaces a pre-existing client id header`() {
        val interceptor = MalClientIdInterceptor(clientId = "fresh-id")
        val requestSlot = slot<Request>()
        val chain: Interceptor.Chain = mockk {
            every { request() } returns Request.Builder()
                .url("https://api.myanimelist.net/v2/anime")
                .header(MalClientIdInterceptor.CLIENT_ID_HEADER, "stale-id")
                .build()
            every { proceed(capture(requestSlot)) } returns mockk(relaxed = true)
        }

        interceptor.intercept(chain)

        assertThat(requestSlot.captured.headers(MalClientIdInterceptor.CLIENT_ID_HEADER))
            .containsExactly("fresh-id")
    }

    @Test
    fun `header name constant matches the MAL specification`() {
        assertThat(MalClientIdInterceptor.CLIENT_ID_HEADER).isEqualTo("X-MAL-CLIENT-ID")
    }
}
