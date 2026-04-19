package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    override fun observeHomeSortState(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[HOME_SORT_STATE_KEY] ?: DEFAULT_HOME_SORT_STATE
        }

    override suspend fun setHomeSortState(state: String) {
        context.dataStore.edit { preferences ->
            preferences[HOME_SORT_STATE_KEY] = state
        }
    }

    override fun observeHomeStatusFilter(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[HOME_STATUS_FILTER_KEY] ?: ""
        }

    override suspend fun setHomeStatusFilter(filter: String) {
        context.dataStore.edit { preferences ->
            preferences[HOME_STATUS_FILTER_KEY] = filter
        }
    }

    override fun observeHomeNotificationFilter(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[HOME_NOTIFICATION_FILTER_KEY] ?: ""
        }

    override suspend fun setHomeNotificationFilter(filter: String) {
        context.dataStore.edit { preferences ->
            preferences[HOME_NOTIFICATION_FILTER_KEY] = filter
        }
    }

    override fun observeSeasonFilter(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[SEASONS_FILTER_KEY] ?: DEFAULT_SEASONS_FILTER
        }

    override suspend fun setSeasonFilter(filter: String) {
        context.dataStore.edit { preferences ->
            preferences[SEASONS_FILTER_KEY] = filter
        }
    }

    override fun observeSearchFilterState(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[SEARCH_FILTER_STATE_KEY] ?: DEFAULT_SEARCH_FILTER_STATE
        }

    override suspend fun setSearchFilterState(state: String) {
        context.dataStore.edit { preferences ->
            preferences[SEARCH_FILTER_STATE_KEY] = state
        }
    }

    override fun observeAnimeDetailTypeFilter(): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[ANIME_DETAIL_TYPE_FILTER_KEY] ?: ""
        }

    override suspend fun setAnimeDetailTypeFilter(filter: String) {
        context.dataStore.edit { preferences ->
            preferences[ANIME_DETAIL_TYPE_FILTER_KEY] = filter
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

    override fun observeIsNotificationDebugInfoEnabled(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[NOTIFICATION_DEBUG_INFO_ENABLED_KEY] ?: false
        }

    override suspend fun setIsNotificationDebugInfoEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_DEBUG_INFO_ENABLED_KEY] = enabled
        }
    }

    companion object {
        private val TITLE_LANGUAGE_KEY = stringPreferencesKey("title_language")
        const val DEFAULT_TITLE_LANGUAGE = "DEFAULT"
        private val HOME_VIEW_MODE_KEY = stringPreferencesKey("home_view_mode")
        const val DEFAULT_HOME_VIEW_MODE = "ANIME"
        private val HOME_SORT_STATE_KEY = stringPreferencesKey("home_sort_state")
        const val DEFAULT_HOME_SORT_STATE = "ALPHABETICAL:true"
        private val SEASONS_FILTER_KEY = stringPreferencesKey("seasons_filter")
        const val DEFAULT_SEASONS_FILTER = "TV"
        private val SEARCH_FILTER_STATE_KEY = stringPreferencesKey("search_filter_state")
        const val DEFAULT_SEARCH_FILTER_STATE = "ALL:ALL:DEFAULT:true"
        private val HOME_STATUS_FILTER_KEY = stringPreferencesKey("home_status_filter")
        private val HOME_NOTIFICATION_FILTER_KEY = stringPreferencesKey("home_notification_filter")
        private val ANIME_DETAIL_TYPE_FILTER_KEY = stringPreferencesKey("anime_detail_type_filter")
        private val DEVELOPER_OPTIONS_ENABLED_KEY = booleanPreferencesKey("developer_options_enabled")
        private val NOTIFICATION_DEBUG_INFO_ENABLED_KEY = booleanPreferencesKey("notification_debug_info_enabled")
    }
}
