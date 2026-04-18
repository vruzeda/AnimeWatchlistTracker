package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import javax.inject.Inject

class SetAnimeDetailTypeFilterUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend operator fun invoke(filter: Set<String>) =
        userPreferencesRepository.setAnimeDetailTypeFilter(filter)
}
