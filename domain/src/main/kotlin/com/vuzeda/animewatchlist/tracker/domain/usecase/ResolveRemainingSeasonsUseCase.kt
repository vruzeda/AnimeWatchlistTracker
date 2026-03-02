package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Resolves the full watch order for an anime and adds any missing seasons. */
class ResolveRemainingSeasonsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val remoteRepository: AnimeRemoteRepository
) {

    suspend operator fun invoke(
        animeId: Long,
        initialMalId: Int,
        status: WatchStatus
    ): Set<Int> {
        val watchOrder = remoteRepository.fetchWatchOrder(initialMalId).getOrNull()
            ?: return emptySet()

        val rootSeason = watchOrder.firstOrNull() ?: return emptySet()
        val rootDetails = remoteRepository.fetchAnimeFullById(rootSeason.malId).getOrNull()

        val existingSeasons = animeRepository.getSeasonsForAnime(animeId)
        val existingMalIds = existingSeasons.map { it.malId }.toSet()

        val resolvedSeasonEntries = watchOrder.mapIndexed { index, seasonData ->
            seasonData to index
        }

        val initialEntry = existingSeasons.firstOrNull { it.malId == initialMalId }
        val correctOrderIndex = resolvedSeasonEntries
            .firstOrNull { it.first.malId == initialMalId }
            ?.second ?: 0

        if (initialEntry != null && initialEntry.orderIndex != correctOrderIndex) {
            animeRepository.updateSeason(initialEntry.copy(orderIndex = correctOrderIndex))
        }

        val remainingSeasons = resolvedSeasonEntries
            .filter { it.first.malId !in existingMalIds }
            .map { (seasonData, index) ->
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
                    orderIndex = index
                )
            }

        if (remainingSeasons.isNotEmpty()) {
            animeRepository.addSeasonsToAnime(animeId, remainingSeasons)
        }

        animeRepository.updateAnime(
            Anime(
                id = animeId,
                title = rootDetails?.title ?: rootSeason.title,
                titleEnglish = rootDetails?.titleEnglish ?: rootSeason.titleEnglish,
                titleJapanese = rootDetails?.titleJapanese ?: rootSeason.titleJapanese,
                imageUrl = rootDetails?.imageUrl ?: rootSeason.imageUrl,
                synopsis = rootDetails?.synopsis,
                genres = rootDetails?.genres ?: emptyList(),
                status = status,
                addedAt = System.currentTimeMillis()
            )
        )

        return watchOrder.map { it.malId }.toSet()
    }
}
