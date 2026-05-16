package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.CoverCacheRepository
import javax.inject.Inject

class GetCoverCacheSizeUseCase @Inject constructor(
    private val coverCacheRepository: CoverCacheRepository
) {

    suspend operator fun invoke(): Long = coverCacheRepository.getCoverCacheSize()
}
