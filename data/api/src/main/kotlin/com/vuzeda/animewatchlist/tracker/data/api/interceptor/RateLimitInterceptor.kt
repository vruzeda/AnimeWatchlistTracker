package com.vuzeda.animewatchlist.tracker.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class RateLimitInterceptor(
    private val minIntervalMs: Long = DEFAULT_INTERVAL_MS
) : Interceptor {

    private var lastRequestTimeMs: Long = 0L
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val host = request.url.host

        if (host == JIKAN_HOST) {
            synchronized(lock) {
                val now = System.currentTimeMillis()
                val elapsed = now - lastRequestTimeMs
                if (elapsed < minIntervalMs) {
                    Thread.sleep(minIntervalMs - elapsed)
                }
                lastRequestTimeMs = System.currentTimeMillis()
            }
        }

        return chain.proceed(request)
    }

    companion object {
        const val JIKAN_HOST = "api.jikan.moe"
        const val DEFAULT_INTERVAL_MS = 334L
    }
}
