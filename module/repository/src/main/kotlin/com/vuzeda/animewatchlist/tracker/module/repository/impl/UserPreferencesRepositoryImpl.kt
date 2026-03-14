package com.vuzeda.animewatchlist.tracker.module.repository.impl

import com.vuzeda.animewatchlist.tracker.module.localdatasource.UserPreferencesLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesLocalDataSource
) : UserPreferencesRepository {

    override fun observeTitleLanguage(): Flow<TitleLanguage> =
        dataSource.observeTitleLanguage().map { value ->
            TitleLanguage.entries.firstOrNull { it.name == value } ?: TitleLanguage.DEFAULT
        }

    override suspend fun setTitleLanguage(language: TitleLanguage) {
        dataSource.setTitleLanguage(language.name)
    }

    override fun observeHomeViewMode(): Flow<HomeViewMode> =
        dataSource.observeHomeViewMode().map { value ->
            HomeViewMode.entries.firstOrNull { it.name == value } ?: HomeViewMode.ANIME
        }

    override suspend fun setHomeViewMode(mode: HomeViewMode) {
        dataSource.setHomeViewMode(mode.name)
    }
}
