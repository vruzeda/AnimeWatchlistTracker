package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Removes an anime and all its seasons from the watchlist. */
class DeleteAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(id: Long) = animeRepository.deleteAnime(id)
}
