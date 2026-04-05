package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Marks or unmarks an individual episode as watched. */
class SetEpisodeWatchedUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {
    suspend operator fun invoke(seasonId: Long, episodeNumber: Int, isWatched: Boolean) =
        seasonRepository.setEpisodeWatched(seasonId, episodeNumber, isWatched)
}
