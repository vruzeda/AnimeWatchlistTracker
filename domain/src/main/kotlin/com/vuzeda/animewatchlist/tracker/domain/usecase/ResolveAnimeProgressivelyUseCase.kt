package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.ProgressiveResolveResult
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ResolveAnimeProgressivelyUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteRepository
) {

    operator fun invoke(malId: Int): Flow<ProgressiveResolveResult> = flow {
        val seasons = remoteRepository.fetchWatchOrder(malId).getOrThrow()
        val rootSeason = seasons.firstOrNull()

        if (rootSeason != null) {
            emit(
                ProgressiveResolveResult(
                    title = rootSeason.title,
                    titleEnglish = rootSeason.titleEnglish,
                    titleJapanese = rootSeason.titleJapanese,
                    imageUrl = rootSeason.imageUrl,
                    synopsis = null,
                    genres = emptyList(),
                    seasons = seasons,
                    isResolvingPrequels = false,
                    isResolvingSequels = false
                )
            )
        }

        val rootDetails = remoteRepository.fetchAnimeFullById(malId).getOrNull()
        if (rootDetails != null) {
            emit(
                ProgressiveResolveResult(
                    title = rootDetails.title,
                    titleEnglish = rootDetails.titleEnglish,
                    titleJapanese = rootDetails.titleJapanese,
                    imageUrl = rootDetails.imageUrl ?: rootSeason?.imageUrl,
                    synopsis = rootDetails.synopsis,
                    genres = rootDetails.genres,
                    seasons = seasons,
                    isResolvingPrequels = false,
                    isResolvingSequels = false
                )
            )
        }
    }
}
