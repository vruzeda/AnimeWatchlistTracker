package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the anime watchlist filtered by notification enabled/disabled status. */
class ObserveAnimeByNotificationUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    operator fun invoke(enabled: Boolean): Flow<List<Anime>> =
        animeRepository.observeByNotificationEnabled(enabled)
}
