package com.vuzeda.animewatchlist.tracker.domain.repository

import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    fun observeTitleLanguage(): Flow<TitleLanguage>

    suspend fun setTitleLanguage(language: TitleLanguage)
}
