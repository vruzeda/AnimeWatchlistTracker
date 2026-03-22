package com.vuzeda.animewatchlist.tracker.module.repository

import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    fun observeTitleLanguage(): Flow<TitleLanguage>

    suspend fun setTitleLanguage(language: TitleLanguage)

    fun observeHomeViewMode(): Flow<HomeViewMode>

    suspend fun setHomeViewMode(mode: HomeViewMode)

    fun observeIsDeveloperOptionsEnabled(): Flow<Boolean>

    suspend fun setIsDeveloperOptionsEnabled(enabled: Boolean)

    fun observeIsNotificationDebugInfoEnabled(): Flow<Boolean>

    suspend fun setIsNotificationDebugInfoEnabled(enabled: Boolean)
}
