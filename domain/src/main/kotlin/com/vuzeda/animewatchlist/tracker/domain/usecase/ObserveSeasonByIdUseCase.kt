package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes a single season by its local database ID. */
class ObserveSeasonByIdUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    operator fun invoke(id: Long): Flow<Season?> = seasonRepository.observeSeasonById(id)
}
