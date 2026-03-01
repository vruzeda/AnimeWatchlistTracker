package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.AnimeSeason
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import javax.inject.Inject

/** Fetches anime for a given year and season from the remote API. */
class GetSeasonAnimeUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteRepository
) {

    suspend operator fun invoke(
        year: Int,
        season: AnimeSeason,
        page: Int = 1
    ): Result<SeasonalAnimePage> = remoteRepository.fetchSeasonAnime(
        year = year,
        season = season,
        page = page
    )
}
