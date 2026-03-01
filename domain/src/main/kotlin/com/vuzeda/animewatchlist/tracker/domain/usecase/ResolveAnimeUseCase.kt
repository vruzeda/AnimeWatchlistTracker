package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.ResolvedSeries
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonData
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Resolves a MAL ID into a full anime series by walking the prequel/sequel chain. */
class ResolveAnimeUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteRepository
) {

    suspend operator fun invoke(malId: Int): Result<ResolvedSeries> = runCatching {
        val startEntry = remoteRepository.fetchAnimeFullById(malId).getOrThrow()

        val root = walkToRoot(startEntry)

        val seasons = walkSeasonChain(root)

        ResolvedSeries(
            title = root.title,
            imageUrl = root.imageUrl,
            synopsis = root.synopsis,
            genres = root.genres,
            seasons = seasons
        )
    }

    private suspend fun walkToRoot(entry: AnimeFullDetails): AnimeFullDetails {
        var current = entry
        while (current.prequels.isNotEmpty()) {
            val prequelId = current.prequels.first().malId
            delay(RATE_LIMIT_DELAY_MS)
            val prequel = remoteRepository.fetchAnimeFullById(prequelId).getOrNull() ?: break
            if (prequel.type !in ALLOWED_TYPES) break
            current = prequel
        }
        return current
    }

    private suspend fun walkSeasonChain(root: AnimeFullDetails): List<SeasonData> {
        val seasons = mutableListOf(root.toSeasonData(orderIndex = 0))
        var current = root
        var index = 1

        while (current.sequels.isNotEmpty()) {
            val sequelId = current.sequels.first().malId
            delay(RATE_LIMIT_DELAY_MS)
            val sequel = remoteRepository.fetchAnimeFullById(sequelId).getOrNull() ?: break
            if (sequel.type in ALLOWED_TYPES) {
                seasons += sequel.toSeasonData(orderIndex = index)
                index++
            }
            current = sequel
        }

        return seasons
    }

    companion object {
        const val RATE_LIMIT_DELAY_MS = 1100L
        val ALLOWED_TYPES = setOf("TV", "Movie")
    }
}

private fun AnimeFullDetails.toSeasonData(orderIndex: Int) = SeasonData(
    malId = malId,
    title = title,
    imageUrl = imageUrl,
    type = type,
    episodeCount = episodes,
    score = score,
    airingStatus = airingStatus,
    synopsis = synopsis,
    genres = genres
)
