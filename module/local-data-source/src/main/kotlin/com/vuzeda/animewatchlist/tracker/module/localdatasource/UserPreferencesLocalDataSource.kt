package com.vuzeda.animewatchlist.tracker.module.localdatasource

import kotlinx.coroutines.flow.Flow

interface UserPreferencesLocalDataSource {
    fun observeTitleLanguage(): Flow<String>
    suspend fun setTitleLanguage(language: String)
    fun observeHomeViewMode(): Flow<String>
    suspend fun setHomeViewMode(mode: String)
    fun observeHomeSortState(): Flow<String>
    suspend fun setHomeSortState(state: String)
    fun observeHomeStatusFilter(): Flow<String>
    suspend fun setHomeStatusFilter(filter: String)
    fun observeHomeNotificationFilter(): Flow<String>
    suspend fun setHomeNotificationFilter(filter: String)
    fun observeIsDeveloperOptionsEnabled(): Flow<Boolean>
    suspend fun setIsDeveloperOptionsEnabled(enabled: Boolean)
    fun observeIsNotificationDebugInfoEnabled(): Flow<Boolean>
    suspend fun setIsNotificationDebugInfoEnabled(enabled: Boolean)
}
