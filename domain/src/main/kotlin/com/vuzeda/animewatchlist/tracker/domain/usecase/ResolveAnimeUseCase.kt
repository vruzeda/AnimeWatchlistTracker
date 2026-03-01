package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.ResolvedSeries
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import javax.inject.Inject

class ResolveAnimeUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteRepository
) {

    suspend operator fun invoke(malId: Int): Result<ResolvedSeries> = runCatching {
        val seasons = remoteRepository.fetchWatchOrder(malId).getOrThrow()
        val rootSeason = seasons.firstOrNull()
            ?: throw IllegalStateException("No seasons found for malId=$malId")

        val rootDetails = remoteRepository.fetchAnimeFullById(rootSeason.malId).getOrNull()

        ResolvedSeries(
            title = rootDetails?.title ?: rootSeason.title,
            titleEnglish = rootDetails?.titleEnglish ?: rootSeason.titleEnglish,
            titleJapanese = rootDetails?.titleJapanese ?: rootSeason.titleJapanese,
            imageUrl = rootDetails?.imageUrl ?: rootSeason.imageUrl,
            synopsis = rootDetails?.synopsis,
            genres = rootDetails?.genres ?: emptyList(),
            seasons = seasons
        )
    }
}
