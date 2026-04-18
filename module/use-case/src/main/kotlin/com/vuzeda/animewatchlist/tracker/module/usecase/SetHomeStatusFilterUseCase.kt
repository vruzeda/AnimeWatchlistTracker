package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import javax.inject.Inject

class SetHomeStatusFilterUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend operator fun invoke(statuses: Set<WatchStatus>) =
        userPreferencesRepository.setHomeStatusFilter(statuses)
}
