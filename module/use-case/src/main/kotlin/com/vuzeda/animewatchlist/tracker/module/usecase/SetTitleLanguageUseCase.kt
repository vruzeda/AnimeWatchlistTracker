package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import javax.inject.Inject

class SetTitleLanguageUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend operator fun invoke(language: TitleLanguage) =
        userPreferencesRepository.setTitleLanguage(language)
}
