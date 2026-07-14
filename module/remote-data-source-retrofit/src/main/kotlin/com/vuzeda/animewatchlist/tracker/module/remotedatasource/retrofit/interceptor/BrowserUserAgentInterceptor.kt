package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class BrowserUserAgentInterceptor(
    private val userAgent: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build()
        )

    companion object {
        const val FALLBACK_BROWSER_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

        private val WEB_VIEW_VERSION_TOKEN_PATTERN = Regex("""Version/\d+(\.\d+)*\s+""")

        fun sanitizeWebViewUserAgent(rawUserAgent: String): String =
            rawUserAgent
                .replace("; wv", "")
                .replace(WEB_VIEW_VERSION_TOKEN_PATTERN, "")
                .trim()
                .ifEmpty { FALLBACK_BROWSER_USER_AGENT }
    }
}
