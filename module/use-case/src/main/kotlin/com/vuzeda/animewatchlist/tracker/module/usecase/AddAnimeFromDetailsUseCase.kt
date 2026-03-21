package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.time.Clock

/** Constructs an Anime with its initial Season from API details and persists them. */
class AddAnimeFromDetailsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val seasonRepository: SeasonRepository,
    private val clock: Clock = Clock.System,
) {

    suspend operator fun invoke(details: AnimeFullDetails, status: WatchStatus): Long {
        val today = clock.todayUtc()
        val watchOrder = animeRepository.fetchWatchOrder(details.malId).getOrNull()
        val orderIndex = resolveOrderIndex(details.malId, watchOrder)

        val clickedSeason = Season(
            malId = details.malId,
            title = details.title,
            titleEnglish = details.titleEnglish,
            titleJapanese = details.titleJapanese,
            imageUrl = details.imageUrl,
            type = details.type,
            episodeCount = details.episodes,
            score = details.score,
            airingStatus = details.airingStatus,
            broadcastInfo = details.broadcastInfo,
            orderIndex = orderIndex,
            status = status,
            lastEpisodeCheckDate = today,
            isInWatchlist = true
        )

        seasonRepository.findAnimeIdBySeasonMalId(details.malId)?.let { return it }

        val existingAnimeId = watchOrder?.firstNotNullOfOrNull { seasonRepository.findAnimeIdBySeasonMalId(it.malId) }
        if (existingAnimeId != null) {
            seasonRepository.addSeasonsToAnime(existingAnimeId, listOf(clickedSeason))
            return existingAnimeId
        }

        val firstSeasonDetails = resolveFirstSeason(details, watchOrder)
        val anime = Anime(
            title = firstSeasonDetails.title,
            titleEnglish = firstSeasonDetails.titleEnglish,
            titleJapanese = firstSeasonDetails.titleJapanese,
            imageUrl = firstSeasonDetails.imageUrl,
            synopsis = firstSeasonDetails.synopsis,
            genres = firstSeasonDetails.genres,
            lastSeasonCheckDate = today,
            addedAt = clock.now().toEpochMilliseconds()
        )
        val allSeasons = buildAllSeasons(clickedSeason, watchOrder)
        return animeRepository.addAnime(anime = anime, seasons = allSeasons)
    }

    private fun buildAllSeasons(clickedSeason: Season, watchOrder: List<SeasonData>?): List<Season> {
        if (watchOrder == null) return listOf(clickedSeason)
        val watchOrderSeasons = watchOrder.mapIndexed { index, seasonData ->
            if (seasonData.malId == clickedSeason.malId) {
                clickedSeason
            } else {
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
                    orderIndex = index,
                    isInWatchlist = false
                )
            }
        }
        return if (watchOrderSeasons.none { it.malId == clickedSeason.malId }) {
            watchOrderSeasons + clickedSeason
        } else {
            watchOrderSeasons
        }
    }

    private suspend fun resolveFirstSeason(
        details: AnimeFullDetails,
        watchOrder: List<SeasonData>?
    ): AnimeFullDetails {
        if (details.prequels.isEmpty()) return details
        if (watchOrder == null) return details

        val firstMalId = watchOrder.firstOrNull { it.isMainSeries }?.malId ?: watchOrder.firstOrNull()?.malId ?: return details
        if (firstMalId == details.malId) return details

        return animeRepository.fetchAnimeFullById(firstMalId).getOrNull() ?: details
    }

    private fun resolveOrderIndex(malId: Int, watchOrder: List<SeasonData>?): Int {
        if (watchOrder == null) return 0
        return watchOrder.indexOfFirst { it.malId == malId }.coerceAtLeast(0)
    }
}

private fun Clock.todayUtc(): LocalDate =
    java.time.Instant.ofEpochMilli(now().toEpochMilliseconds())
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
