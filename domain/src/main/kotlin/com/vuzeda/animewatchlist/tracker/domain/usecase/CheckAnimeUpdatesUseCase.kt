package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.domain.model.KnownSequel
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

            val lastAiredEpisode = animeRepository.fetchLastAiredEpisodeNumber(malId)
                .getOrNull()
            delay(RATE_LIMIT_DELAY_MS)

            val previousAiredCount = anime.lastCheckedAiredEpisodeCount
            if (previousAiredCount != null &&
                lastAiredEpisode != null &&
                lastAiredEpisode > previousAiredCount
            ) {
                updates += AnimeUpdate.NewEpisodes(
                    anime = anime,
                    latestAiredEpisode = lastAiredEpisode
                )
            }

            val detailsResult = animeRepository.fetchAnimeFullDetails(malId)
            val details = detailsResult.getOrNull()
            delay(RATE_LIMIT_DELAY_MS)

            val knownSequelsMap = anime.knownSequels.associateBy { it.malId }
            val updatedSequels = mutableListOf<KnownSequel>()

            if (details != null) {
                for (sequel in details.sequels) {
                    val known = knownSequelsMap[sequel.malId]
                    if (known != null && known.notified) {
                        updatedSequels += known
                        continue
                    }

                    val basicInfo = animeRepository.fetchAnimeBasicInfo(sequel.malId).getOrNull()
                    delay(RATE_LIMIT_DELAY_MS)

                    val isAiringOrConfirmed = basicInfo != null &&
                        (basicInfo.status == STATUS_CURRENTLY_AIRING || basicInfo.airedFrom != null)

                    if (isAiringOrConfirmed) {
                        updates += AnimeUpdate.NewSeason(
                            anime = anime,
                            sequelMalId = sequel.malId,
                            sequelTitle = sequel.title
                        )
                        updatedSequels += KnownSequel(malId = sequel.malId, notified = true)
                    } else {
                        updatedSequels += KnownSequel(malId = sequel.malId, notified = false)
                    }
                }
            } else {
                updatedSequels += anime.knownSequels
            }

            animeRepository.updateNotificationData(
                id = anime.id,
                lastCheckedAiredEpisodeCount = lastAiredEpisode ?: previousAiredCount,
                knownSequels = updatedSequels
            )
        }

        return updates
    }

    companion object {
        const val RATE_LIMIT_DELAY_MS = 1100L
        const val STATUS_CURRENTLY_AIRING = "Currently Airing"
    }
}
