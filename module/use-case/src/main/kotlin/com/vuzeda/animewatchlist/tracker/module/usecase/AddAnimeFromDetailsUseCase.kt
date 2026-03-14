package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject
import kotlin.time.Clock

/** Constructs an Anime with its initial Season from API details and persists them. */
class AddAnimeFromDetailsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val remoteRepository: AnimeRemoteDataSource,
    private val clock: Clock = Clock.System,
) {

    suspend operator fun invoke(details: AnimeFullDetails, status: WatchStatus): Long {
        val watchOrder = remoteRepository.fetchWatchOrder(details.malId).getOrNull()
        val firstSeasonDetails = resolveFirstSeason(details, watchOrder)
        val orderIndex = resolveOrderIndex(details.malId, watchOrder)

        val anime = Anime(
            title = firstSeasonDetails.title,
            titleEnglish = firstSeasonDetails.titleEnglish,
            titleJapanese = firstSeasonDetails.titleJapanese,
            imageUrl = firstSeasonDetails.imageUrl,
            synopsis = firstSeasonDetails.synopsis,
            genres = firstSeasonDetails.genres,
            status = status,
            addedAt = clock.now().toEpochMilliseconds()
        )
        val season = Season(
            malId = details.malId,
            title = details.title,
            titleEnglish = details.titleEnglish,
            titleJapanese = details.titleJapanese,
            imageUrl = details.imageUrl,
            type = details.type,
            episodeCount = details.episodes,
            score = details.score,
            airingStatus = details.airingStatus,
            orderIndex = orderIndex
        )
        return animeRepository.addAnime(anime = anime, seasons = listOf(season))
    }

    private suspend fun resolveFirstSeason(
        details: AnimeFullDetails,
        watchOrder: List<SeasonData>?
    ): AnimeFullDetails {
        if (details.prequels.isEmpty()) return details
        if (watchOrder == null) return details

        val firstMalId = watchOrder.firstOrNull { it.isMainSeries }?.malId ?: watchOrder.firstOrNull()?.malId ?: return details
        if (firstMalId == details.malId) return details

        return remoteRepository.fetchAnimeFullById(firstMalId).getOrNull() ?: details
    }

    private fun resolveOrderIndex(malId: Int, watchOrder: List<SeasonData>?): Int {
        if (watchOrder == null) return 0
        return watchOrder.indexOfFirst { it.malId == malId }.coerceAtLeast(0)
    }
}
