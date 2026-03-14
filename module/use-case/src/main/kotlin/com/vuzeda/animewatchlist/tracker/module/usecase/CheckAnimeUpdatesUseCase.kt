package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Checks all notification-enabled anime and seasons for new episodes and new seasons. */
class CheckAnimeUpdatesUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val seasonRepository: SeasonRepository,
    private val remoteRepository: AnimeRemoteDataSource
) {

    suspend operator fun invoke(): List<AnimeUpdate> {
        val updates = mutableListOf<AnimeUpdate>()
        val checkedSeasonIds = mutableSetOf<Long>()

        val notifiedAnime = animeRepository.getNotificationEnabledAnime()
        for (anime in notifiedAnime) {
            val seasons = seasonRepository.getSeasonsForAnime(anime.id)
            if (seasons.isEmpty()) continue

            val shouldCheckEpisodes = anime.notificationType == NotificationType.NEW_EPISODES ||
                anime.notificationType == NotificationType.BOTH
            val shouldCheckSeasons = anime.notificationType == NotificationType.NEW_SEASONS ||
                anime.notificationType == NotificationType.BOTH

            if (shouldCheckEpisodes) {
                for (season in seasons) {
                    checkedSeasonIds += season.id
                    checkNewEpisodes(season)?.let { latestEpisode ->
                        updates += AnimeUpdate.NewEpisodes(
                            anime = anime,
                            season = season,
                            latestAiredEpisode = latestEpisode
                        )
                    }
                }
            }

            if (shouldCheckSeasons) {
                checkNewSeasons(anime, seasons)?.let { updates += it }
            }
        }

        val animeCache = notifiedAnime.associateBy { it.id }.toMutableMap()
        val perSeasonNotified = seasonRepository.getSeasonsWithEpisodeNotifications()
        for (season in perSeasonNotified) {
            if (season.id in checkedSeasonIds) continue
            var anime = animeCache[season.animeId]
            if (anime == null) {
                anime = animeRepository.getAnimeById(season.animeId) ?: continue
                animeCache[season.animeId] = anime
            }
            val latestEpisode = checkNewEpisodes(season) ?: continue
            updates += AnimeUpdate.NewEpisodes(
                anime = anime,
                season = season,
                latestAiredEpisode = latestEpisode
            )
        }

        return updates
    }

    private suspend fun checkNewEpisodes(season: Season): Int? {
        val lastAiredEpisode = remoteRepository.fetchLastAiredEpisodeNumber(season.malId)
            .getOrNull() ?: return null
        val previousCount = season.lastCheckedAiredEpisodeCount

        seasonRepository.updateSeasonNotificationData(
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
        anime: com.vuzeda.animewatchlist.tracker.module.domain.Anime,
        existingSeasons: List<Season>
    ): AnimeUpdate.NewSeason? {
        val lastSeason = existingSeasons.maxByOrNull { it.orderIndex } ?: return null
        val details = remoteRepository.fetchAnimeFullById(lastSeason.malId).getOrNull()
            ?: return null

        val knownMalIds = existingSeasons.map { it.malId }.toSet()

        for (sequel in details.sequels) {
            if (sequel.malId in knownMalIds) continue

            val sequelDetails = remoteRepository.fetchAnimeFullById(sequel.malId).getOrNull()

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
        const val STATUS_CURRENTLY_AIRING = "Currently Airing"
        const val STATUS_FINISHED_AIRING = "Finished Airing"
        val ALLOWED_TYPES = setOf("TV", "Movie")
    }
}
