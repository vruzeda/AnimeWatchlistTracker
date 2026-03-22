package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Immediately schedules a one-time anime update check. */
class TriggerAnimeUpdateUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    operator fun invoke() = animeRepository.scheduleImmediateAnimeUpdate()
}
