package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import com.vuzeda.animewatchlist.tracker.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetTitleLanguageUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend operator fun invoke(language: TitleLanguage) =
        userPreferencesRepository.setTitleLanguage(language)
}
