package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes a single anime by its local database ID. */
class ObserveAnimeByIdUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    operator fun invoke(id: Long): Flow<Anime?> = animeRepository.observeById(id)
}
