package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import javax.inject.Inject

/** Fetches anime for a given year and season from the remote API. */
class GetSeasonAnimeUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteDataSource
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
