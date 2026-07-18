package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import javax.inject.Inject

/** Returns an installation ID used in feedback reports. */
class GetInstallationIdUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(): String =
        userPreferencesRepository.getInstallationId()
}
