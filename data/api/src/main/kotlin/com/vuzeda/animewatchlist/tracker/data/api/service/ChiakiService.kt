package com.vuzeda.animewatchlist.tracker.data.api.service

import com.vuzeda.animewatchlist.tracker.data.api.dto.ChiakiWatchOrderEntryDto

interface ChiakiService {

    suspend fun fetchWatchOrder(malId: Int): List<ChiakiWatchOrderEntryDto>

    companion object {
        const val BASE_URL = "https://chiaki.site/"
    }
}
