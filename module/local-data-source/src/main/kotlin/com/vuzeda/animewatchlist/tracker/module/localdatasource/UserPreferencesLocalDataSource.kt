package com.vuzeda.animewatchlist.tracker.module.localdatasource

import kotlinx.coroutines.flow.Flow

interface UserPreferencesLocalDataSource {
    fun observeTitleLanguage(): Flow<String>
    suspend fun setTitleLanguage(language: String)
    fun observeHomeViewMode(): Flow<String>
    suspend fun setHomeViewMode(mode: String)
    fun observeIsDeveloperOptionsEnabled(): Flow<Boolean>
    suspend fun setIsDeveloperOptionsEnabled(enabled: Boolean)
}
