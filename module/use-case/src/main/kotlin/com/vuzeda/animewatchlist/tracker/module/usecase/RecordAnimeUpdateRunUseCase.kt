package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Records the current time as the last successful AnimeUpdateWorker run. */
class RecordAnimeUpdateRunUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke() = animeRepository.recordAnimeUpdateRun()
}
