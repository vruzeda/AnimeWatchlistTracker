package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTitleLanguageUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    operator fun invoke(): Flow<TitleLanguage> =
        userPreferencesRepository.observeTitleLanguage()
}
