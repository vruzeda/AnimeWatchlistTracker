package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class MalClientIdInterceptor(
    private val clientId: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().newBuilder()
                .header(CLIENT_ID_HEADER, clientId)
                .build()
        )

    companion object {
        const val CLIENT_ID_HEADER = "X-MAL-CLIENT-ID"
    }
}
