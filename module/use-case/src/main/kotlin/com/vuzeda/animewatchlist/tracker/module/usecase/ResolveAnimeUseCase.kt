package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.ResolvedSeries
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ResolveAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(malId: Int): Result<ResolvedSeries> = try {
        val seasons = animeRepository.fetchWatchOrder(malId).getOrThrow()
        val rootSeason = seasons.firstOrNull { it.isMainSeries } ?: seasons.firstOrNull()
            ?: throw IllegalStateException("No seasons found for malId=$malId")

        val rootDetails = animeRepository.fetchAnimeFullById(rootSeason.malId).getOrNull()

        Result.success(ResolvedSeries(
            title = rootDetails?.title ?: rootSeason.title,
            titleEnglish = rootDetails?.titleEnglish ?: rootSeason.titleEnglish,
            titleJapanese = rootDetails?.titleJapanese ?: rootSeason.titleJapanese,
            imageUrl = rootDetails?.imageUrl ?: rootSeason.imageUrl,
            synopsis = rootDetails?.synopsis,
            genres = rootDetails?.genres ?: emptyList(),
            seasons = seasons
        ))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
