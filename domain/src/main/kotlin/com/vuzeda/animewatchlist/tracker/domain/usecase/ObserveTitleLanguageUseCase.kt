package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import com.vuzeda.animewatchlist.tracker.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTitleLanguageUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    operator fun invoke(): Flow<TitleLanguage> =
        userPreferencesRepository.observeTitleLanguage()
}
