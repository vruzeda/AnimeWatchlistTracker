package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Updates an existing anime in the watchlist. */
class UpdateAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(anime: Anime) = animeRepository.updateAnime(anime)
}
