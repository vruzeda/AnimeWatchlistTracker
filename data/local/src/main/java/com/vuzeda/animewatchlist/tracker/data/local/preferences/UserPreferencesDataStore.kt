package com.vuzeda.animewatchlist.tracker.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(
    private val context: Context
) {

    fun observeTitleLanguage(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[TITLE_LANGUAGE_KEY] ?: DEFAULT_TITLE_LANGUAGE
        }

    suspend fun setTitleLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[TITLE_LANGUAGE_KEY] = language
        }
    }

    fun observeHomeViewMode(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[HOME_VIEW_MODE_KEY] ?: DEFAULT_HOME_VIEW_MODE
        }

    suspend fun setHomeViewMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[HOME_VIEW_MODE_KEY] = mode
        }
    }

    companion object {
        private val TITLE_LANGUAGE_KEY = stringPreferencesKey("title_language")
        const val DEFAULT_TITLE_LANGUAGE = "DEFAULT"
        private val HOME_VIEW_MODE_KEY = stringPreferencesKey("home_view_mode")
        const val DEFAULT_HOME_VIEW_MODE = "ANIME"
    }
}
