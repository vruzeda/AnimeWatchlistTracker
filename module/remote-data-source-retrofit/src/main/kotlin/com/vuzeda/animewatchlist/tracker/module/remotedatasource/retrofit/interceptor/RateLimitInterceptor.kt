package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.interceptor

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

        var waitMs = 0L
        if (host == TENRAI_HOST) {
            synchronized(lock) {
                val now = System.currentTimeMillis()
                val elapsed = now - lastRequestTimeMs
                if (elapsed < minIntervalMs) {
                    waitMs = minIntervalMs - elapsed
                }
                lastRequestTimeMs = System.currentTimeMillis()
            }
            if (waitMs > 0L) {
                Thread.sleep(waitMs)
            }
        }

        return chain.proceed(request)
    }

    companion object {
        const val TENRAI_HOST = "api.tenrai.org"
        const val DEFAULT_INTERVAL_MS = 334L
    }
}
