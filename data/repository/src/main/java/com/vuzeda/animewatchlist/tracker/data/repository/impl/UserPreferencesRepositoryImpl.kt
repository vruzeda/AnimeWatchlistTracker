package com.vuzeda.animewatchlist.tracker.data.repository.impl

import com.vuzeda.animewatchlist.tracker.data.local.preferences.UserPreferencesDataStore
import com.vuzeda.animewatchlist.tracker.domain.model.HomeViewMode
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import com.vuzeda.animewatchlist.tracker.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {

    override fun observeTitleLanguage(): Flow<TitleLanguage> =
        dataStore.observeTitleLanguage().map { value ->
            TitleLanguage.entries.firstOrNull { it.name == value } ?: TitleLanguage.DEFAULT
        }

    override suspend fun setTitleLanguage(language: TitleLanguage) {
        dataStore.setTitleLanguage(language.name)
    }

    override fun observeHomeViewMode(): Flow<HomeViewMode> =
        dataStore.observeHomeViewMode().map { value ->
            HomeViewMode.entries.firstOrNull { it.name == value } ?: HomeViewMode.ANIME
        }

    override suspend fun setHomeViewMode(mode: HomeViewMode) {
        dataStore.setHomeViewMode(mode.name)
    }
}
