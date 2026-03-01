package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import javax.inject.Inject

/** Fetches full details for a single MAL season entry. */
class FetchSeasonDetailUseCase @Inject constructor(
    private val remoteRepository: AnimeRemoteRepository
) {

    suspend operator fun invoke(malId: Int): Result<AnimeFullDetails> =
        remoteRepository.fetchAnimeFullById(malId)
}
