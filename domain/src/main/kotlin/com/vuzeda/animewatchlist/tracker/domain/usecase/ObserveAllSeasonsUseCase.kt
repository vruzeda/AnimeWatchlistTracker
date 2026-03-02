package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllSeasonsUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    operator fun invoke(): Flow<List<Season>> =
        seasonRepository.observeAllSeasons()
}
