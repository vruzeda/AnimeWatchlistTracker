package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Fetches the latest season metadata from the API and updates the local database record. */
class RefreshSeasonDataUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(season: Season): Result<Unit> {
        val details = animeRepository.fetchAnimeFullById(season.malId).getOrElse {
            return Result.failure(it)
        }
        seasonRepository.updateSeason(
            season.copy(
                title = details.title,
                titleEnglish = details.titleEnglish,
                titleJapanese = details.titleJapanese,
                imageUrl = details.imageUrl ?: season.imageUrl,
                type = details.type,
                episodeCount = details.episodes ?: season.episodeCount,
                score = details.score ?: season.score,
                airingStatus = details.airingStatus ?: season.airingStatus,
                broadcastInfo = details.broadcastInfo,
                broadcastDay = details.broadcastDay,
                broadcastTime = details.broadcastTime,
                broadcastTimezone = details.broadcastTimezone,
                streamingLinks = details.streamingLinks,
                airingSeasonName = details.airingSeasonName ?: season.airingSeasonName,
                airingSeasonYear = details.airingSeasonYear ?: season.airingSeasonYear
            )
        )
        return Result.success(Unit)
    }
}
