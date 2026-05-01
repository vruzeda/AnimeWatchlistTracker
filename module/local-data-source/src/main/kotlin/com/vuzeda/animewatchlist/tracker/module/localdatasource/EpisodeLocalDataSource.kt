package com.vuzeda.animewatchlist.tracker.module.localdatasource

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo

interface EpisodeLocalDataSource {
    suspend fun getEpisodes(malId: Int): List<EpisodeInfo>
    suspend fun upsertEpisodes(malId: Int, episodes: List<EpisodeInfo>)
}
