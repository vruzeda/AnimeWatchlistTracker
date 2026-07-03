package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Fetches and persists airingSeasonName/airingSeasonYear for watchlist seasons that are missing
 * them. Also fills in any other null metadata fields available from the API response.
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
                    titleEnglish = season.titleEnglish ?: details.titleEnglish,
                    titleJapanese = season.titleJapanese ?: details.titleJapanese,
                    imageUrl = season.imageUrl ?: details.imageUrl,
                    episodeCount = season.episodeCount ?: details.episodes,
                    score = season.score ?: details.score,
                    airingStatus = season.airingStatus ?: details.airingStatus,
                    broadcastDay = season.broadcastDay ?: details.broadcastDay,
                    broadcastTime = season.broadcastTime ?: details.broadcastTime,
                    broadcastTimezone = season.broadcastTimezone ?: details.broadcastTimezone,
                    broadcastInfo = season.broadcastInfo ?: details.broadcastInfo,
                    streamingLinks = season.streamingLinks.ifEmpty { details.streamingLinks },
                    airingSeasonName = details.airingSeasonName ?: season.airingSeasonName,
                    airingSeasonYear = details.airingSeasonYear ?: season.airingSeasonYear
                )
            )
        }
    }
}
