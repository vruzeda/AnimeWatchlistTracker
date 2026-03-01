package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the anime watchlist, optionally filtered by status. */
class ObserveAnimeListUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    operator fun invoke(status: WatchStatus? = null): Flow<List<Anime>> =
        if (status != null) animeRepository.observeByStatus(status)
        else animeRepository.observeAll()
}
