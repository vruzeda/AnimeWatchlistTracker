package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Fetches and persists airingSeasonName/airingSeasonYear for watchlist seasons that are missing
 * them. Also fills in broadcast fields if they are null. Introduces a delay between API calls
 * to respect Jikan's rate limit.
 */
class BackfillMissingAiringSeasonUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke() {
        val seasons = seasonRepository.observeAllSeasons().first()
            .filter { it.isInWatchlist && it.airingSeasonName == null && it.malId > 0 }

        for (season in seasons) {
            val details = animeRepository.fetchAnimeFullById(season.malId).getOrNull() ?: continue
            seasonRepository.updateSeason(
                season.copy(
                    airingSeasonName = details.airingSeasonName,
                    airingSeasonYear = details.airingSeasonYear,
                    broadcastDay = season.broadcastDay ?: details.broadcastDay,
                    broadcastTime = season.broadcastTime ?: details.broadcastTime,
                    broadcastTimezone = season.broadcastTimezone ?: details.broadcastTimezone,
                    broadcastInfo = season.broadcastInfo ?: details.broadcastInfo
                )
            )
            delay(JIKAN_REQUEST_DELAY_MS)
        }
    }

    companion object {
        private const val JIKAN_REQUEST_DELAY_MS = 500L
    }
}
