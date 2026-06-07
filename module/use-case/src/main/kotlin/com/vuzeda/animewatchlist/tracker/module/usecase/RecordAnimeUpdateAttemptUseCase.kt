package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Records the outcome of an AnimeUpdateWorker run — success, failure, or retry. */
class RecordAnimeUpdateAttemptUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(result: AnimeUpdateResult) = animeRepository.recordAnimeUpdateAttempt(result)
}
