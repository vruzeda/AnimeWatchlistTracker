package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Removes an anime from the watchlist by ID. */
class DeleteAnimeFromWatchlistUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(id: Long) = animeRepository.deleteAnime(id)
}
