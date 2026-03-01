package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Retrieves the current list of seasons for an anime. */
class GetSeasonsForAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(animeId: Long): List<Season> =
        animeRepository.getSeasonsForAnime(animeId)
}
