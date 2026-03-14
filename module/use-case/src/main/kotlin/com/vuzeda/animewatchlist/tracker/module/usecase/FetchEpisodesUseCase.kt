package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Fetches a page of episodes for a MAL anime entry. */
class FetchEpisodesUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(malId: Int, page: Int = 1): Result<EpisodePage> =
        animeRepository.fetchAnimeEpisodes(malId = malId, page = page)
}
