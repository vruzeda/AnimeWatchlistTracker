package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Checks all notification-enabled anime for new episodes and new seasons. */
class CheckAnimeUpdatesUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(): List<AnimeUpdate> {
        val notifiedAnime = animeRepository.getNotifiedAnime()
        val updates = mutableListOf<AnimeUpdate>()

        for (anime in notifiedAnime) {
            val malId = anime.malId ?: continue
            val detailsResult = animeRepository.fetchAnimeFullDetails(malId)
            val details = detailsResult.getOrNull() ?: continue

            val previousEpisodeCount = anime.lastCheckedEpisodeCount
            val currentEpisodeCount = details.episodes

            if (previousEpisodeCount != null &&
                currentEpisodeCount != null &&
                currentEpisodeCount > previousEpisodeCount
            ) {
                updates += AnimeUpdate.NewEpisodes(
                    anime = anime,
                    previousCount = previousEpisodeCount,
                    currentCount = currentEpisodeCount
                )
            }

            val knownSequelIds = anime.knownSequelMalIds.toSet()
            for (sequel in details.sequels) {
                if (sequel.malId !in knownSequelIds) {
                    updates += AnimeUpdate.NewSeason(
                        anime = anime,
                        sequelMalId = sequel.malId,
                        sequelTitle = sequel.title
                    )
                }
            }

            val newSequelIds = details.sequels.map { it.malId }
            animeRepository.updateNotificationData(
                id = anime.id,
                lastCheckedEpisodeCount = currentEpisodeCount ?: previousEpisodeCount,
                knownSequelMalIds = newSequelIds
            )

            delay(RATE_LIMIT_DELAY_MS)
        }

        return updates
    }

    companion object {
        const val RATE_LIMIT_DELAY_MS = 400L
    }
}
