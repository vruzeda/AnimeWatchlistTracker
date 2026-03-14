package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHomeViewModeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    operator fun invoke(): Flow<HomeViewMode> =
        userPreferencesRepository.observeHomeViewMode()
}
