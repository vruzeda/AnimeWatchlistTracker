package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Marks all given episodes as watched for a season. */
class SetAllEpisodesWatchedUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {
    suspend operator fun invoke(seasonId: Long, episodeNumbers: List<Int>) {
        episodeNumbers.forEach { episodeNumber ->
            seasonRepository.setEpisodeWatched(seasonId, episodeNumber, isWatched = true)
        }
    }
}
