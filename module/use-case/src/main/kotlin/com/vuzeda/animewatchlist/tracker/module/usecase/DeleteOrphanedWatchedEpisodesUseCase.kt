package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Removes watched episode records for episode numbers beyond a season's confirmed episode count. */
class DeleteOrphanedWatchedEpisodesUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {
    suspend operator fun invoke(seasonId: Long, episodeCount: Int) =
        seasonRepository.deleteOrphanedWatchedEpisodes(seasonId, episodeCount)
}
