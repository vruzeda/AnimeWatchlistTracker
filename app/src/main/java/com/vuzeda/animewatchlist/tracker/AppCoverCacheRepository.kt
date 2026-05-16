package com.vuzeda.animewatchlist.tracker

import android.content.Context
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import com.vuzeda.animewatchlist.tracker.module.repository.CoverCacheRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@OptIn(ExperimentalCoilApi::class)
class AppCoverCacheRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : CoverCacheRepository {

    override suspend fun getCoverCacheSize(): Long =
        Coil.imageLoader(context).diskCache?.size ?: 0L

    override suspend fun clearCoverCache() {
        Coil.imageLoader(context).also {
            it.diskCache?.clear()
            it.memoryCache?.clear()
        }
    }
}
