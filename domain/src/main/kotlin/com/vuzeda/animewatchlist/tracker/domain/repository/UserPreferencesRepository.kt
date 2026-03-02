package com.vuzeda.animewatchlist.tracker.domain.repository

import com.vuzeda.animewatchlist.tracker.domain.model.HomeViewMode
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    fun observeTitleLanguage(): Flow<TitleLanguage>

    suspend fun setTitleLanguage(language: TitleLanguage)

    fun observeHomeViewMode(): Flow<HomeViewMode>

    suspend fun setHomeViewMode(mode: HomeViewMode)
}
