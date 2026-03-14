package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import javax.inject.Inject

class SetHomeViewModeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend operator fun invoke(mode: HomeViewMode) =
        userPreferencesRepository.setHomeViewMode(mode)
}
