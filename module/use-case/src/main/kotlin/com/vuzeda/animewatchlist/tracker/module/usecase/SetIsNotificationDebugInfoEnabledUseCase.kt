package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import javax.inject.Inject

class SetIsNotificationDebugInfoEnabledUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend operator fun invoke(enabled: Boolean) =
        userPreferencesRepository.setIsNotificationDebugInfoEnabled(enabled)
}
