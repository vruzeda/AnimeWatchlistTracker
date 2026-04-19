package com.vuzeda.animewatchlist.tracker.module.repository

import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortState
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.SearchSortState
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonsSortState
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    fun observeTitleLanguage(): Flow<TitleLanguage>

    suspend fun setTitleLanguage(language: TitleLanguage)

    fun observeHomeViewMode(): Flow<HomeViewMode>

    suspend fun setHomeViewMode(mode: HomeViewMode)

    fun observeHomeSortState(): Flow<HomeSortState>

    suspend fun setHomeSortState(state: HomeSortState)

    fun observeSeasonsSortState(): Flow<SeasonsSortState>

    suspend fun setSeasonsSortState(state: SeasonsSortState)

    fun observeSearchSortState(): Flow<SearchSortState>

    suspend fun setSearchSortState(state: SearchSortState)

    fun observeHomeStatusFilter(): Flow<Set<WatchStatus>>

    suspend fun setHomeStatusFilter(statuses: Set<WatchStatus>)

    fun observeHomeNotificationFilter(): Flow<Boolean?>

    suspend fun setHomeNotificationFilter(enabled: Boolean?)

    fun observeAnimeDetailTypeFilter(): Flow<Set<String>>

    suspend fun setAnimeDetailTypeFilter(filter: Set<String>)

    fun observeIsDeveloperOptionsEnabled(): Flow<Boolean>

    suspend fun setIsDeveloperOptionsEnabled(enabled: Boolean)

    fun observeIsNotificationDebugInfoEnabled(): Flow<Boolean>

    suspend fun setIsNotificationDebugInfoEnabled(enabled: Boolean)
}
