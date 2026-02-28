package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Fetches a single anime from the remote API by its MAL ID. */
class FetchAnimeByMalIdUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(malId: Int): Result<Anime> =
        animeRepository.fetchAnimeByMalId(malId)
}
