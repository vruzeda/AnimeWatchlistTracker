package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vuzeda.animewatchlist.tracker.module.localdatasource.UserPreferencesLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(
    private val context: Context
) : UserPreferencesLocalDataSource {

    override fun observeTitleLanguage(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[TITLE_LANGUAGE_KEY] ?: DEFAULT_TITLE_LANGUAGE
        }

    override suspend fun setTitleLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[TITLE_LANGUAGE_KEY] = language
        }
    }

    override fun observeHomeViewMode(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[HOME_VIEW_MODE_KEY] ?: DEFAULT_HOME_VIEW_MODE
        }

    override suspend fun setHomeViewMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[HOME_VIEW_MODE_KEY] = mode
        }
    }

    override fun observeIsDeveloperOptionsEnabled(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[DEVELOPER_OPTIONS_ENABLED_KEY] ?: false
        }

    override suspend fun setIsDeveloperOptionsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEVELOPER_OPTIONS_ENABLED_KEY] = enabled
        }
    }

    companion object {
        private val TITLE_LANGUAGE_KEY = stringPreferencesKey("title_language")
        const val DEFAULT_TITLE_LANGUAGE = "DEFAULT"
        private val HOME_VIEW_MODE_KEY = stringPreferencesKey("home_view_mode")
        const val DEFAULT_HOME_VIEW_MODE = "ANIME"
        private val DEVELOPER_OPTIONS_ENABLED_KEY = booleanPreferencesKey("developer_options_enabled")
    }
}
