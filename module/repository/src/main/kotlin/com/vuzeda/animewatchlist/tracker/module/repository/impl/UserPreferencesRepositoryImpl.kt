package com.vuzeda.animewatchlist.tracker.module.repository.impl

import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortOption
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortState
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.localdatasource.UserPreferencesLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesLocalDataSource
) : UserPreferencesRepository {

    override fun observeTitleLanguage(): Flow<TitleLanguage> =
        dataSource.observeTitleLanguage().map { value ->
            TitleLanguage.entries.firstOrNull { it.name == value } ?: TitleLanguage.DEFAULT
        }

    override suspend fun setTitleLanguage(language: TitleLanguage) {
        dataSource.setTitleLanguage(language.name)
    }

    override fun observeHomeViewMode(): Flow<HomeViewMode> =
        dataSource.observeHomeViewMode().map { value ->
            HomeViewMode.entries.firstOrNull { it.name == value } ?: HomeViewMode.ANIME
        }

    override suspend fun setHomeViewMode(mode: HomeViewMode) {
        dataSource.setHomeViewMode(mode.name)
    }

    override fun observeHomeSortState(): Flow<HomeSortState> =
        dataSource.observeHomeSortState().map { value ->
            val parts = value.split(":")
            val option = HomeSortOption.entries.firstOrNull { it.name == parts.getOrNull(0) }
                ?: HomeSortOption.ALPHABETICAL
            val ascending = parts.getOrNull(1)?.toBooleanStrictOrNull() ?: option.defaultAscending
            HomeSortState(option, ascending)
        }

    override suspend fun setHomeSortState(state: HomeSortState) {
        dataSource.setHomeSortState("${state.option.name}:${state.isAscending}")
    }

    override fun observeHomeStatusFilter(): Flow<WatchStatus?> =
        dataSource.observeHomeStatusFilter().map { value ->
            if (value.isEmpty()) null else WatchStatus.entries.firstOrNull { it.name == value }
        }

    override suspend fun setHomeStatusFilter(status: WatchStatus?) {
        dataSource.setHomeStatusFilter(status?.name ?: "")
    }

    override fun observeHomeNotificationFilter(): Flow<Boolean?> =
        dataSource.observeHomeNotificationFilter().map { value ->
            if (value.isEmpty()) null else value.toBooleanStrictOrNull()
        }

    override suspend fun setHomeNotificationFilter(enabled: Boolean?) {
        dataSource.setHomeNotificationFilter(enabled?.toString() ?: "")
    }

    override fun observeIsDeveloperOptionsEnabled(): Flow<Boolean> =
        dataSource.observeIsDeveloperOptionsEnabled()

    override suspend fun setIsDeveloperOptionsEnabled(enabled: Boolean) {
        dataSource.setIsDeveloperOptionsEnabled(enabled)
    }

    override fun observeIsNotificationDebugInfoEnabled(): Flow<Boolean> =
        dataSource.observeIsNotificationDebugInfoEnabled()

    override suspend fun setIsNotificationDebugInfoEnabled(enabled: Boolean) {
        dataSource.setIsNotificationDebugInfoEnabled(enabled)
    }
}
