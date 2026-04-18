package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import java.time.LocalDate
import javax.inject.Inject

/** Fetches the watch order from the API and inserts any seasons not yet stored locally as non-watchlist entries. */
class RefreshAnimeSeasonsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(animeId: Long) {
        val existingSeasons = seasonRepository.getSeasonsForAnime(animeId)
        if (existingSeasons.isEmpty()) return

        val anyMalId = existingSeasons.first().malId
        val watchOrder = animeRepository.fetchWatchOrder(anyMalId).getOrNull() ?: return

        val existingMalIds = existingSeasons.map { it.malId }.toSet()
        val newSeasons = watchOrder
            .filterNot { it.malId in existingMalIds }
            .map { seasonData ->
                Season(
                    malId = seasonData.malId,
                    title = seasonData.title,
                    titleEnglish = seasonData.titleEnglish,
                    titleJapanese = seasonData.titleJapanese,
                    imageUrl = seasonData.imageUrl,
                    type = seasonData.type,
                    episodeCount = seasonData.episodeCount,
                    score = seasonData.score,
                    airingStatus = seasonData.airingStatus,
                    airingSeasonName = seasonData.startDate?.toAiringSeasonName(),
                    airingSeasonYear = seasonData.startDate?.year,
                    orderIndex = watchOrder.indexOfFirst { it.malId == seasonData.malId },
                    isInWatchlist = false
                )
            }

        if (newSeasons.isEmpty()) return
        seasonRepository.addSeasonsToAnime(animeId, newSeasons)
    }
}

private fun LocalDate.toAiringSeasonName(): String = when (monthValue) {
    in 1..3 -> "winter"
    in 4..6 -> "spring"
    in 7..9 -> "summer"
    else    -> "fall"
}
