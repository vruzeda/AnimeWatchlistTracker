package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Checks all notification-enabled anime for new episodes and new seasons. */
class CheckAnimeUpdatesUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val remoteRepository: AnimeRemoteRepository
) {

    suspend operator fun invoke(): List<AnimeUpdate> {
        val notifiedAnime = animeRepository.getNotificationEnabledAnime()
        val updates = mutableListOf<AnimeUpdate>()

        for (anime in notifiedAnime) {
            val seasons = animeRepository.getSeasonsForAnime(anime.id)
            if (seasons.isEmpty()) continue

            for (season in seasons) {
                checkNewEpisodes(season)?.let { update ->
                    updates += AnimeUpdate.NewEpisodes(
                        anime = anime,
                        season = season,
                        latestAiredEpisode = update
                    )
                }
                delay(RATE_LIMIT_DELAY_MS)
            }

            checkNewSeasons(anime, seasons)?.let { updates += it }
        }

        return updates
    }

    private suspend fun checkNewEpisodes(season: Season): Int? {
        val lastAiredEpisode = remoteRepository.fetchLastAiredEpisodeNumber(season.malId)
            .getOrNull() ?: return null
        val previousCount = season.lastCheckedAiredEpisodeCount

        animeRepository.updateSeasonNotificationData(
            seasonId = season.id,
            lastCheckedAiredEpisodeCount = lastAiredEpisode
        )

        return if (previousCount != null && lastAiredEpisode > previousCount) {
            lastAiredEpisode
        } else {
            null
        }
    }

    private suspend fun checkNewSeasons(
        anime: com.vuzeda.animewatchlist.tracker.domain.model.Anime,
        existingSeasons: List<Season>
    ): AnimeUpdate.NewSeason? {
        val lastSeason = existingSeasons.maxByOrNull { it.orderIndex } ?: return null
        val details = remoteRepository.fetchAnimeFullById(lastSeason.malId).getOrNull()
            ?: return null
        delay(RATE_LIMIT_DELAY_MS)

        val knownMalIds = existingSeasons.map { it.malId }.toSet()

        for (sequel in details.sequels) {
            if (sequel.malId in knownMalIds) continue

            val sequelDetails = remoteRepository.fetchAnimeFullById(sequel.malId).getOrNull()
            delay(RATE_LIMIT_DELAY_MS)

            if (sequelDetails != null && sequelDetails.type in ALLOWED_TYPES) {
                val isConfirmed = sequelDetails.airingStatus == STATUS_CURRENTLY_AIRING ||
                    sequelDetails.airingStatus == STATUS_FINISHED_AIRING

                if (isConfirmed) {
                    return AnimeUpdate.NewSeason(
                        anime = anime,
                        sequelMalId = sequel.malId,
                        sequelTitle = sequelDetails.title
                    )
                }
            }
        }
        return null
    }

    companion object {
        const val RATE_LIMIT_DELAY_MS = 1100L
        const val STATUS_CURRENTLY_AIRING = "Currently Airing"
        const val STATUS_FINISHED_AIRING = "Finished Airing"
        val ALLOWED_TYPES = setOf("TV", "Movie")
    }
}
