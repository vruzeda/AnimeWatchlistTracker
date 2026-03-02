package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.HomeViewMode
import com.vuzeda.animewatchlist.tracker.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetHomeViewModeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend operator fun invoke(mode: HomeViewMode) =
        userPreferencesRepository.setHomeViewMode(mode)
}
