package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAnimeDetailTypeFilterUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    operator fun invoke(): Flow<Set<String>> =
        userPreferencesRepository.observeAnimeDetailTypeFilter()
}
