package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import javax.inject.Inject

/** Fetches a page of episodes for a MAL anime entry. */
class FetchEpisodesUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteDataSource
) {

    suspend operator fun invoke(malId: Int, page: Int = 1): Result<EpisodePage> =
        remoteRepository.fetchAnimeEpisodes(malId = malId, page = page)
}
