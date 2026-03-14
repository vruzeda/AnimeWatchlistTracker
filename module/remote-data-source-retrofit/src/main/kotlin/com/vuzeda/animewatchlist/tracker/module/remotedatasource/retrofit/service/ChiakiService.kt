package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ChiakiWatchOrderEntryDto

interface ChiakiService {

    suspend fun fetchWatchOrder(malId: Int): List<ChiakiWatchOrderEntryDto>

    companion object {
        const val BASE_URL = "https://chiaki.site/"
    }
}
