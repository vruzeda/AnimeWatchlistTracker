package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.ProgressiveResolveResult
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonData
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/** Resolves a MAL ID into a full anime series progressively, emitting after each season is discovered. */
class ResolveAnimeProgressivelyUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteRepository
) {

    operator fun invoke(malId: Int): Flow<ProgressiveResolveResult> = flow {
        val startEntry = remoteRepository.fetchAnimeFullById(malId).getOrThrow()

        val hasPrequels = startEntry.prequels.isNotEmpty()
        val hasSequels = startEntry.sequels.isNotEmpty()

        val prequelChain = mutableListOf<AnimeFullDetails>()

        emit(
            buildResult(
                prequelChain = prequelChain,
                startEntry = startEntry,
                sequelChain = emptyList(),
                isResolvingPrequels = hasPrequels,
                isResolvingSequels = hasSequels
            )
        )

        var current = startEntry
        while (current.prequels.isNotEmpty()) {
            val prequelId = current.prequels.first().malId
            delay(RATE_LIMIT_DELAY_MS)
            val prequel = remoteRepository.fetchAnimeFullById(prequelId).getOrNull() ?: break
            if (prequel.type !in ALLOWED_TYPES) break
            prequelChain.add(0, prequel)
            current = prequel

            emit(
                buildResult(
                    prequelChain = prequelChain,
                    startEntry = startEntry,
                    sequelChain = emptyList(),
                    isResolvingPrequels = current.prequels.isNotEmpty(),
                    isResolvingSequels = hasSequels
                )
            )
        }

        val sequelChain = mutableListOf<AnimeFullDetails>()
        current = startEntry
        while (current.sequels.isNotEmpty()) {
            val sequelId = current.sequels.first().malId
            delay(RATE_LIMIT_DELAY_MS)
            val sequel = remoteRepository.fetchAnimeFullById(sequelId).getOrNull() ?: break
            if (sequel.type in ALLOWED_TYPES) {
                sequelChain.add(sequel)
            }
            current = sequel

            emit(
                buildResult(
                    prequelChain = prequelChain,
                    startEntry = startEntry,
                    sequelChain = sequelChain,
                    isResolvingPrequels = false,
                    isResolvingSequels = current.sequels.isNotEmpty()
                )
            )
        }

        if (!hasPrequels && !hasSequels) return@flow

        emit(
            buildResult(
                prequelChain = prequelChain,
                startEntry = startEntry,
                sequelChain = sequelChain,
                isResolvingPrequels = false,
                isResolvingSequels = false
            )
        )
    }

    companion object {
        const val RATE_LIMIT_DELAY_MS = 1100L
        val ALLOWED_TYPES = setOf("TV", "Movie")
    }
}

private fun buildResult(
    prequelChain: List<AnimeFullDetails>,
    startEntry: AnimeFullDetails,
    sequelChain: List<AnimeFullDetails>,
    isResolvingPrequels: Boolean,
    isResolvingSequels: Boolean
): ProgressiveResolveResult {
    val root = prequelChain.firstOrNull() ?: startEntry
    val allEntries = prequelChain + startEntry + sequelChain
    val seasons = allEntries.mapIndexed { index, entry ->
        SeasonData(
            malId = entry.malId,
            title = entry.title,
            imageUrl = entry.imageUrl,
            type = entry.type,
            episodeCount = entry.episodes,
            score = entry.score,
            airingStatus = entry.airingStatus,
            synopsis = entry.synopsis,
            genres = entry.genres
        )
    }
    return ProgressiveResolveResult(
        title = root.title,
        imageUrl = root.imageUrl,
        synopsis = root.synopsis,
        genres = root.genres,
        seasons = seasons,
        isResolvingPrequels = isResolvingPrequels,
        isResolvingSequels = isResolvingSequels
    )
}
