package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.vuzeda.animewatchlist.tracker.module.localdatasource.UserPreferencesLocalDataSource
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferencesDataStore(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesLocalDataSource {

    private val safePreferences: Flow<Preferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }

    override fun observeTitleLanguage(): Flow<String> =
        safePreferences.map { preferences ->
            preferences[TITLE_LANGUAGE_KEY] ?: DEFAULT_TITLE_LANGUAGE
        }

    override suspend fun setTitleLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[TITLE_LANGUAGE_KEY] = language
        }
    }

    override fun observeHomeViewMode(): Flow<String> =
        safePreferences.map { preferences ->
            preferences[HOME_VIEW_MODE_KEY] ?: DEFAULT_HOME_VIEW_MODE
        }

    override suspend fun setHomeViewMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[HOME_VIEW_MODE_KEY] = mode
        }
    }

    override fun observeHomeSortState(): Flow<String> =
        safePreferences.map { preferences ->
            val option = preferences[HOME_SORT_OPTION_KEY] ?: "ALPHABETICAL"
            val ascending = preferences[HOME_SORT_ASCENDING_KEY] ?: true
            "$option:$ascending"
        }

    override suspend fun setHomeSortState(state: String) {
        dataStore.edit { preferences ->
            val parts = state.split(":")
            preferences[HOME_SORT_OPTION_KEY] = parts.getOrNull(0) ?: "ALPHABETICAL"
            preferences[HOME_SORT_ASCENDING_KEY] = parts.getOrNull(1)?.toBooleanStrictOrNull() ?: true
        }
    }

    override fun observeHomeStatusFilter(): Flow<String> =
        safePreferences.map { preferences ->
            (preferences[HOME_STATUS_FILTER_SET_KEY] ?: emptySet()).joinToString(",")
        }

    override suspend fun setHomeStatusFilter(filter: String) {
        dataStore.edit { preferences ->
            preferences[HOME_STATUS_FILTER_SET_KEY] = if (filter.isEmpty()) emptySet() else filter.split(",").toSet()
        }
    }

    override fun observeHomeNotificationFilter(): Flow<String> =
        safePreferences.map { preferences ->
            preferences[HOME_NOTIFICATION_FILTER_KEY] ?: ""
        }

    override suspend fun setHomeNotificationFilter(filter: String) {
        dataStore.edit { preferences ->
            preferences[HOME_NOTIFICATION_FILTER_KEY] = filter
        }
    }

    override fun observeSeasonFilter(): Flow<String> =
        safePreferences.map { preferences ->
            preferences[SEASONS_FILTER_KEY] ?: DEFAULT_SEASONS_FILTER
        }

    override suspend fun setSeasonFilter(filter: String) {
        dataStore.edit { preferences ->
            preferences[SEASONS_FILTER_KEY] = filter
        }
    }

    override fun observeSearchFilterState(): Flow<String> =
        safePreferences.map { preferences ->
            val type = preferences[SEARCH_FILTER_TYPE_KEY] ?: "ALL"
            val status = preferences[SEARCH_FILTER_STATUS_KEY] ?: "ALL"
            val orderBy = preferences[SEARCH_FILTER_ORDER_BY_KEY] ?: "DEFAULT"
            val ascending = preferences[SEARCH_FILTER_ASCENDING_KEY] ?: true
            "$type:$status:$orderBy:$ascending"
        }

    override suspend fun setSearchFilterState(state: String) {
        dataStore.edit { preferences ->
            val parts = state.split(":")
            preferences[SEARCH_FILTER_TYPE_KEY] = parts.getOrNull(0) ?: "ALL"
            preferences[SEARCH_FILTER_STATUS_KEY] = parts.getOrNull(1) ?: "ALL"
            preferences[SEARCH_FILTER_ORDER_BY_KEY] = parts.getOrNull(2) ?: "DEFAULT"
            preferences[SEARCH_FILTER_ASCENDING_KEY] = parts.getOrNull(3)?.toBooleanStrictOrNull() ?: true
        }
    }

    override fun observeAnimeDetailTypeFilter(): Flow<String> =
        safePreferences.map { preferences ->
            (preferences[ANIME_DETAIL_TYPE_FILTER_SET_KEY] ?: emptySet()).joinToString(",")
        }

    override suspend fun setAnimeDetailTypeFilter(filter: String) {
        dataStore.edit { preferences ->
            preferences[ANIME_DETAIL_TYPE_FILTER_SET_KEY] = if (filter.isEmpty()) emptySet() else filter.split(",").toSet()
        }
    }

    override fun observeIsDeveloperOptionsEnabled(): Flow<Boolean> =
        safePreferences.map { preferences ->
            preferences[DEVELOPER_OPTIONS_ENABLED_KEY] ?: false
        }

    override suspend fun setIsDeveloperOptionsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DEVELOPER_OPTIONS_ENABLED_KEY] = enabled
        }
    }

    override fun observeIsNotificationDebugInfoEnabled(): Flow<Boolean> =
        safePreferences.map { preferences ->
            preferences[NOTIFICATION_DEBUG_INFO_ENABLED_KEY] ?: false
        }

    override suspend fun setIsNotificationDebugInfoEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_DEBUG_INFO_ENABLED_KEY] = enabled
        }
    }

    override fun observeIsOfflineCoverCachingEnabled(): Flow<Boolean> =
        safePreferences.map { preferences ->
            preferences[OFFLINE_COVER_CACHING_KEY] ?: true
        }

    override suspend fun setIsOfflineCoverCachingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[OFFLINE_COVER_CACHING_KEY] = enabled
        }
    }

    override fun observeAnimeProvider(): Flow<String> =
        safePreferences.map { preferences ->
            preferences[ANIME_PROVIDER_KEY] ?: DEFAULT_ANIME_PROVIDER
        }

    override suspend fun setAnimeProvider(provider: String) {
        dataStore.edit { preferences ->
            preferences[ANIME_PROVIDER_KEY] = provider
        }
    }

    companion object {
        private val TITLE_LANGUAGE_KEY = stringPreferencesKey("title_language")
        const val DEFAULT_TITLE_LANGUAGE = "DEFAULT"
        private val HOME_VIEW_MODE_KEY = stringPreferencesKey("home_view_mode")
        const val DEFAULT_HOME_VIEW_MODE = "ANIME"
        private val HOME_SORT_OPTION_KEY = stringPreferencesKey("home_sort_option")
        private val HOME_SORT_ASCENDING_KEY = booleanPreferencesKey("home_sort_ascending")
        private val SEASONS_FILTER_KEY = stringPreferencesKey("seasons_filter")
        const val DEFAULT_SEASONS_FILTER = "TV"
        private val SEARCH_FILTER_TYPE_KEY = stringPreferencesKey("search_filter_type")
        private val SEARCH_FILTER_STATUS_KEY = stringPreferencesKey("search_filter_status")
        private val SEARCH_FILTER_ORDER_BY_KEY = stringPreferencesKey("search_filter_order_by")
        private val SEARCH_FILTER_ASCENDING_KEY = booleanPreferencesKey("search_filter_ascending")
        private val HOME_STATUS_FILTER_SET_KEY = stringSetPreferencesKey("home_status_filter_set")
        private val HOME_NOTIFICATION_FILTER_KEY = stringPreferencesKey("home_notification_filter")
        private val ANIME_DETAIL_TYPE_FILTER_SET_KEY = stringSetPreferencesKey("anime_detail_type_filter_set")
        private val DEVELOPER_OPTIONS_ENABLED_KEY = booleanPreferencesKey("developer_options_enabled")
        private val NOTIFICATION_DEBUG_INFO_ENABLED_KEY = booleanPreferencesKey("notification_debug_info_enabled")
        private val OFFLINE_COVER_CACHING_KEY = booleanPreferencesKey("offline_cover_caching_enabled")
        private val ANIME_PROVIDER_KEY = stringPreferencesKey("anime_provider")
        const val DEFAULT_ANIME_PROVIDER = "JIKAN"
    }
}
