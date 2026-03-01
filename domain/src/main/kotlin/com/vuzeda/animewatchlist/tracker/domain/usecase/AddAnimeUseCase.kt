package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Adds a resolved anime with its seasons to the local watchlist. */
class AddAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(
        anime: Anime,
        seasons: List<Season>,
        status: WatchStatus
    ): Long = animeRepository.addAnime(
        anime = anime.copy(
            status = status,
            addedAt = System.currentTimeMillis()
        ),
        seasons = seasons
    )
}
