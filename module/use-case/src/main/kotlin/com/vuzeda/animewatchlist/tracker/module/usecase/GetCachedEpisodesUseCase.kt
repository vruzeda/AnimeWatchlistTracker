package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Returns locally cached episode metadata for a MAL anime entry. */
class GetCachedEpisodesUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(malId: Int): List<EpisodeInfo> =
        animeRepository.getCachedEpisodes(malId)
}
