package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import javax.inject.Inject

/** Finds the local season ID for a given MAL ID. */
class FindSeasonIdByMalIdUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(malId: Int): Long? =
        seasonRepository.findSeasonIdByMalId(malId)
}
