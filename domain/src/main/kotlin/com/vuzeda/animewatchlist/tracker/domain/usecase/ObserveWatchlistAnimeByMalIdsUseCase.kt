package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes watchlist anime matching the given MAL IDs. */
class ObserveWatchlistAnimeByMalIdsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    operator fun invoke(malIds: List<Int>): Flow<List<Anime>> =
        animeRepository.observeAnimeByMalIds(malIds)
}
