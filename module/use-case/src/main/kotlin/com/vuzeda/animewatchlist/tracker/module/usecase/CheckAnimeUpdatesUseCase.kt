package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.time.Clock

/** Checks all notification-enabled anime and seasons for new episodes and new seasons. */
class CheckAnimeUpdatesUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val seasonRepository: SeasonRepository,
    private val clock: Clock = Clock.System
) {

    suspend operator fun invoke(): List<AnimeUpdate> {
        val today = clock.todayUtc()
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
                for (season in seasons.filter { it.isInWatchlist }) {
                    checkedSeasonIds += season.id
                    val newCount = checkNewEpisodes(season, today)
                    if (newCount != null) {
                        updates += AnimeUpdate.NewEpisodes(
                            anime = anime,
                            season = season,
                            newEpisodeCount = newCount
                        )
                    }
                }
            }

            if (shouldCheckSeasons) {
                checkNewSeasons(anime, seasons, today)?.let { updates += it }
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
            val newCount = checkNewEpisodes(season, today) ?: continue
            updates += AnimeUpdate.NewEpisodes(
                anime = anime,
                season = season,
                newEpisodeCount = newCount
            )
        }

        return updates
    }

    private suspend fun checkNewEpisodes(season: Season, today: LocalDate): Int? {
        val isFirstRun = season.lastEpisodeCheckDate == null
        val after = season.lastEpisodeCheckDate ?: LocalDate.MIN
        val episodes = animeRepository.fetchEpisodesAiredBetween(
            malId = season.malId,
            after = after,
            upTo = today,
            startingFromEpisode = season.lastCheckedAiredEpisodeCount
        ).getOrNull() ?: return null

        val lastEpisodeNumber = episodes.maxByOrNull { it.number }?.number
        if (lastEpisodeNumber != null) {
            seasonRepository.updateSeasonNotificationData(
                seasonId = season.id,
                lastCheckedAiredEpisodeCount = lastEpisodeNumber
            )
        }

        val lastAiredDate = episodes.mapNotNull { parseLocalDate(it.aired) }.maxOrNull()
        when {
            lastAiredDate != null -> seasonRepository.updateLastEpisodeCheckDate(season.id, lastAiredDate)
            isFirstRun -> seasonRepository.updateLastEpisodeCheckDate(season.id, LocalDate.MIN)
        }

        if (isFirstRun) return null

        return if (episodes.isNotEmpty()) episodes.size else null
    }

    private suspend fun checkNewSeasons(
        anime: Anime,
        seasons: List<Season>,
        today: LocalDate
    ): AnimeUpdate.NewSeason? {
        val watchlistedSeasons = seasons.filter { it.isInWatchlist }
        val lastSeason = watchlistedSeasons.maxByOrNull { it.orderIndex } ?: return null
        val watchOrder = animeRepository.fetchWatchOrder(lastSeason.malId).getOrNull() ?: return null

        val knownMalIds = seasons.map { it.malId }.toSet()

        if (anime.lastSeasonCheckDate == null) {
            val lastKnownStartDate = watchOrder
                .filter { it.malId in knownMalIds }
                .mapNotNull { it.startDate }
                .filter { !it.isAfter(today) }
                .maxOrNull()
            animeRepository.updateLastSeasonCheckDate(anime.id, lastKnownStartDate ?: today)
            return null
        }

        for (entry in watchOrder) {
            if (entry.malId in knownMalIds) continue

            val startDate = entry.startDate ?: continue
            if (!startDate.isAfter(anime.lastSeasonCheckDate)) continue
            if (startDate.isAfter(today)) continue

            animeRepository.updateLastSeasonCheckDate(anime.id, startDate)
            return AnimeUpdate.NewSeason(
                anime = anime,
                sequelMalId = entry.malId,
                sequelTitle = entry.title
            )
        }
        return null
    }
}

private fun Clock.todayUtc(): LocalDate =
    java.time.Instant.ofEpochMilli(now().toEpochMilliseconds())
        .atZone(ZoneOffset.UTC)
        .toLocalDate()

private fun parseLocalDate(aired: String?): LocalDate? {
    if (aired == null) return null
    return try {
        LocalDate.parse(aired.take(10))
    } catch (_: Exception) {
        null
    }
}
