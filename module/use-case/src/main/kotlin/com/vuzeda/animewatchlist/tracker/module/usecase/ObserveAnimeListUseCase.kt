package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
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
