package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.AiringStatus
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.parseAiredDateFromString
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
                    checkNewEpisodes(anime, season, today)?.let { updates += it }
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

            checkNewEpisodes(anime, season, today)?.let { updates += it }
        }

        return updates
    }

    private suspend fun checkNewEpisodes(
        anime: Anime,
        season: Season,
        today: LocalDate
    ): AnimeUpdate.NewEpisodes? {
        if (season.lastEpisodeCheckPerformedDate == today) return null

        if (season.airingStatus == AiringStatus.FINISHED_AIRING.displayName && season.latestKnownEpisodeAirDate != null) return null

        val episodeCount = season.episodeCount
        val checkedCount = season.lastCheckedAiredEpisodeCount ?: 0
        if (episodeCount != null && episodeCount > 0 && checkedCount >= episodeCount) return null

        val isFirstRun = season.latestKnownEpisodeAirDate == null
        val after = season.latestKnownEpisodeAirDate ?: LocalDate.MIN
        val episodesResult = animeRepository.fetchEpisodesAiredBetween(
            malId = season.malId,
            after = after,
            upTo = today,
            startingFromEpisode = season.lastCheckedAiredEpisodeCount
        )
        val episodes = when (val err = episodesResult.exceptionOrNull()) {
            is DataError.RateLimited, is DataError.Network -> throw err
            null -> episodesResult.getOrThrow()
            else -> return null
        }

        val lastEpisodeNumber = episodes.maxByOrNull { it.number }?.number
        if (lastEpisodeNumber != null) {
            seasonRepository.updateSeasonNotificationData(
                seasonId = season.id,
                lastCheckedAiredEpisodeCount = lastEpisodeNumber
            )
        }

        val lastAiredDate = episodes.mapNotNull { parseAiredDateFromString(it.aired) }.maxOrNull()
        when {
            lastAiredDate != null -> seasonRepository.updateLatestKnownEpisodeAirDate(season.id, lastAiredDate)
            isFirstRun -> seasonRepository.updateLatestKnownEpisodeAirDate(season.id, LocalDate.MIN)
        }

        seasonRepository.updateLastEpisodeCheckPerformedDate(season.id, today)

        if (isFirstRun) return null

        if (episodes.isEmpty()) return null

        val watchedNumbers = seasonRepository.getWatchedEpisodeNumbers(season.id)
        val unwatchedEpisodes = episodes.filter { it.number !in watchedNumbers }
        if (unwatchedEpisodes.isEmpty()) return null

        return AnimeUpdate.NewEpisodes(
            anime = anime,
            season = season,
            newEpisodeCount = unwatchedEpisodes.size
        )
    }

    private suspend fun checkNewSeasons(
        anime: Anime,
        seasons: List<Season>,
        today: LocalDate
    ): AnimeUpdate.NewSeason? {
        if (anime.lastSeasonCheckPerformedDate == today) return null

        val watchlistedSeasons = seasons.filter { it.isInWatchlist }
        val lastSeason = watchlistedSeasons.maxByOrNull { it.orderIndex } ?: return null
        val watchOrderResult = animeRepository.fetchWatchOrder(lastSeason.malId)
        val watchOrder = when (val err = watchOrderResult.exceptionOrNull()) {
            is DataError.RateLimited, is DataError.Network -> throw err
            null -> watchOrderResult.getOrThrow()
            else -> return null
        }

        val knownMalIds = seasons.map { it.malId }.toSet()

        if (anime.latestKnownSeasonStartDate == null) {
            val lastKnownStartDate = watchOrder
                .filter { it.malId in knownMalIds }
                .mapNotNull { it.startDate }
                .filter { !it.isAfter(today) }
                .maxOrNull()
            animeRepository.updateLatestKnownSeasonStartDate(anime.id, lastKnownStartDate ?: today)
            animeRepository.updateLastSeasonCheckPerformedDate(anime.id, today)
            return null
        }

        for (entry in watchOrder) {
            if (entry.malId in knownMalIds) continue

            val startDate = entry.startDate ?: continue
            if (!startDate.isAfter(anime.latestKnownSeasonStartDate)) continue
            if (startDate.isAfter(today)) continue

            animeRepository.updateLatestKnownSeasonStartDate(anime.id, startDate)
            animeRepository.updateLastSeasonCheckPerformedDate(anime.id, today)
            return AnimeUpdate.NewSeason(
                anime = anime,
                sequelMalId = entry.malId,
                sequelTitle = entry.title,
                sequelTitleEnglish = entry.titleEnglish,
                sequelTitleJapanese = entry.titleJapanese,
            )
        }

        animeRepository.updateLastSeasonCheckPerformedDate(anime.id, today)
        return null
    }
}

private fun Clock.todayUtc(): LocalDate =
    java.time.Instant.ofEpochMilli(now().toEpochMilliseconds())
        .atZone(ZoneOffset.UTC)
        .toLocalDate()

