package com.vuzeda.animewatchlist.tracker.data.local

import kotlinx.coroutines.flow.Flow

interface UserPreferencesLocalDataSource {
    fun observeTitleLanguage(): Flow<String>
    suspend fun setTitleLanguage(language: String)
    fun observeHomeViewMode(): Flow<String>
    suspend fun setHomeViewMode(mode: String)
}
