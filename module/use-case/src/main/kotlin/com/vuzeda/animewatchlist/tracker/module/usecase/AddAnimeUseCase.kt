package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject
import kotlin.time.Clock

/** Adds a resolved anime with its seasons to the local watchlist. */
class AddAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val clock: Clock = Clock.System,
) {

    suspend operator fun invoke(
        anime: Anime,
        seasons: List<Season>,
        status: WatchStatus
    ): Long = animeRepository.addAnime(
        anime = anime.copy(
            status = status,
            addedAt = clock.now().toEpochMilliseconds(),
        ),
        seasons = seasons
    )
}
