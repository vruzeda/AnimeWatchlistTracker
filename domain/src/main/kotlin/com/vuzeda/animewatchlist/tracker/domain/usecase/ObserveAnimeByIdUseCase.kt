package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes a single anime by its local database ID. */
class ObserveAnimeByIdUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    operator fun invoke(id: Long): Flow<Anime?> = animeRepository.observeById(id)
}
