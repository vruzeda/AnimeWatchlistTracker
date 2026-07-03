package com.vuzeda.animewatchlist.tracker.module.covercache

import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.CachePolicy
import java.util.concurrent.atomic.AtomicBoolean

class DiskCachePolicyInterceptor(private val isEnabled: AtomicBoolean) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult =
        chain.proceed(
            if (isEnabled.get()) chain.request
            else chain.request.newBuilder().diskCachePolicy(CachePolicy.DISABLED).build()
        )
}
