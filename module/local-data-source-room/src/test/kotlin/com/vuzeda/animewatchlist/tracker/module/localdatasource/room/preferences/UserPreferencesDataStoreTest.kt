package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.IOException

class UserPreferencesDataStoreTest {

    private class FailingDataStore(private val failure: Throwable) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw failure }
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            throw failure
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val state = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = state
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }

    private fun corruptedDataStore() =
        UserPreferencesDataStore(FailingDataStore(IOException("corrupted preferences file")))

    @Test
    fun `observeTitleLanguage emits default when preferences file is corrupted`() = runTest {
        val language = corruptedDataStore().observeTitleLanguage().first()

        assertThat(language).isEqualTo(UserPreferencesDataStore.DEFAULT_TITLE_LANGUAGE)
    }

    @Test
    fun `observeHomeSortState emits defaults when preferences file is corrupted`() = runTest {
        val sortState = corruptedDataStore().observeHomeSortState().first()

        assertThat(sortState).isEqualTo("ALPHABETICAL:true")
    }

    @Test
    fun `observeIsOfflineCoverCachingEnabled emits default when preferences file is corrupted`() = runTest {
        val enabled = corruptedDataStore().observeIsOfflineCoverCachingEnabled().first()

        assertThat(enabled).isTrue()
    }

    @Test
    fun `observeHomeStatusFilter emits empty filter when preferences file is corrupted`() = runTest {
        val filter = corruptedDataStore().observeHomeStatusFilter().first()

        assertThat(filter).isEmpty()
    }

    @Test
    fun `observeAnimeProvider emits default when preferences file is corrupted`() = runTest {
        val provider = corruptedDataStore().observeAnimeProvider().first()

        assertThat(provider).isEqualTo(UserPreferencesDataStore.DEFAULT_ANIME_PROVIDER)
    }

    @Test
    fun `setAnimeProvider round-trips through observeAnimeProvider`() = runTest {
        val dataStore = UserPreferencesDataStore(InMemoryDataStore())

        dataStore.setAnimeProvider("MAL")

        assertThat(dataStore.observeAnimeProvider().first()).isEqualTo("MAL")
    }

    @Test
    fun `non-IO failures propagate to collectors`() = runTest {
        val dataStore = UserPreferencesDataStore(FailingDataStore(IllegalStateException("bug")))

        val thrown = runCatching { dataStore.observeTitleLanguage().first() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `setTitleLanguage round-trips through observeTitleLanguage`() = runTest {
        val dataStore = UserPreferencesDataStore(InMemoryDataStore())

        dataStore.setTitleLanguage("JAPANESE")

        assertThat(dataStore.observeTitleLanguage().first()).isEqualTo("JAPANESE")
    }

    @Test
    fun `setHomeSortState round-trips option and direction`() = runTest {
        val dataStore = UserPreferencesDataStore(InMemoryDataStore())

        dataStore.setHomeSortState("SCORE:false")

        assertThat(dataStore.observeHomeSortState().first()).isEqualTo("SCORE:false")
    }

    @Test
    fun `setSearchFilterState round-trips all four fields`() = runTest {
        val dataStore = UserPreferencesDataStore(InMemoryDataStore())

        dataStore.setSearchFilterState("TV:AIRING:SCORE:false")

        assertThat(dataStore.observeSearchFilterState().first()).isEqualTo("TV:AIRING:SCORE:false")
    }

    @Test
    fun `setHomeStatusFilter round-trips a single status`() = runTest {
        val dataStore = UserPreferencesDataStore(InMemoryDataStore())

        dataStore.setHomeStatusFilter("WATCHING")

        assertThat(dataStore.observeHomeStatusFilter().first()).isEqualTo("WATCHING")
    }

    @Test
    fun `setHomeSortState keeps ascending default on malformed input`() = runTest {
        val dataStore = UserPreferencesDataStore(InMemoryDataStore())

        dataStore.setHomeSortState("garbage")

        assertThat(dataStore.observeHomeSortState().first()).isEqualTo("garbage:true")
    }
}
