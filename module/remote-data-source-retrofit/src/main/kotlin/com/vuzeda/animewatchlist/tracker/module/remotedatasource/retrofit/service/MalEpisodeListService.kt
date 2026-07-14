package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeListPageDto

interface MalEpisodeListService {

    suspend fun fetchEpisodePage(malId: Int, page: Int): MalEpisodeListPageDto

    companion object {
        const val BASE_URL = "https://myanimelist.net"
        const val PAGE_SIZE = 100
    }
}
