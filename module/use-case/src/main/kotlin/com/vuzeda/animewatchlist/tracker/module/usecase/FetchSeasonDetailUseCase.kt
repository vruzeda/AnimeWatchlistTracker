package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.AnimeRemoteDataSource
import javax.inject.Inject

/** Fetches full details for a single MAL season entry. */
class FetchSeasonDetailUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteDataSource
) {

    suspend operator fun invoke(malId: Int): Result<AnimeFullDetails> =
        remoteRepository.fetchAnimeFullById(malId)
}
