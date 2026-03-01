package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.EpisodePage
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import javax.inject.Inject

/** Fetches a page of episodes for a MAL anime entry. */
class FetchEpisodesUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteRepository
) {

    suspend operator fun invoke(malId: Int, page: Int = 1): Result<EpisodePage> =
        remoteRepository.fetchAnimeEpisodes(malId = malId, page = page)
}
