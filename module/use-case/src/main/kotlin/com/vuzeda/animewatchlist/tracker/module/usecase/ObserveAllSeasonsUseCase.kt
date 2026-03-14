package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllSeasonsUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    operator fun invoke(): Flow<List<Season>> =
        seasonRepository.observeAllSeasons()
}
