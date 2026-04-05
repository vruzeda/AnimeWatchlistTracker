package com.vuzeda.animewatchlist.tracker.module.repository.impl

import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.localdatasource.SeasonLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.WatchedEpisodeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.module.repository.TransactionRunner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class SeasonRepositoryImpl @Inject constructor(
    private val seasonLocalDataSource: SeasonLocalDataSource,
    private val watchedEpisodeLocalDataSource: WatchedEpisodeLocalDataSource,
    private val transactionRunner: TransactionRunner
) : SeasonRepository {

    override fun observeAllSeasons(): Flow<List<Season>> =
        combine(
            seasonLocalDataSource.observeAll(),
            watchedEpisodeLocalDataSource.observeWatchedCountsForAllSeasons()
        ) { seasons, watchedCounts ->
            seasons.map { it.copy(watchedEpisodeCount = watchedCounts[it.id] ?: 0) }
        }

    override fun observeAllSeasonMalIds(): Flow<Set<Int>> =
        seasonLocalDataSource.observeAllMalIds().map { it.toSet() }

    override fun observeSeasonsForAnime(animeId: Long): Flow<List<Season>> =
        combine(
            seasonLocalDataSource.observeByAnimeId(animeId),
            watchedEpisodeLocalDataSource.observeWatchedCountsForAllSeasons()
        ) { seasons, watchedCounts ->
            seasons.map { it.copy(watchedEpisodeCount = watchedCounts[it.id] ?: 0) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSeasonById(id: Long): Flow<Season?> =
        seasonLocalDataSource.observeById(id).flatMapLatest { season ->
            if (season == null) return@flatMapLatest flowOf(null)
            watchedEpisodeLocalDataSource.observeWatchedEpisodeNumbers(id)
                .map { watchedEpisodes -> season.copy(watchedEpisodeCount = watchedEpisodes.size) }
        }

    override suspend fun findAnimeIdBySeasonMalId(malId: Int): Long? =
        seasonLocalDataSource.findByMalId(malId)?.animeId

    override suspend fun findSeasonIdByMalId(malId: Int): Long? =
        seasonLocalDataSource.findByMalId(malId)?.id

    override suspend fun getSeasonsForAnime(animeId: Long): List<Season> =
        seasonLocalDataSource.getByAnimeId(animeId)

    override suspend fun deleteSeason(id: Long) {
        seasonLocalDataSource.deleteById(id)
    }

    override suspend fun addSeasonsToAnime(animeId: Long, seasons: List<Season>) {
        transactionRunner.runInTransaction {
            seasonLocalDataSource.insertAll(seasons.map { it.copy(animeId = animeId) })
        }
    }

    override suspend fun updateSeason(season: Season) {
        seasonLocalDataSource.update(season)
    }

    override suspend fun updateSeasonNotificationData(
        seasonId: Long,
        lastCheckedAiredEpisodeCount: Int?
    ) {
        seasonLocalDataSource.updateNotificationData(
            seasonId = seasonId,
            count = lastCheckedAiredEpisodeCount
        )
    }

    override suspend fun toggleSeasonEpisodeNotifications(seasonId: Long, enabled: Boolean) {
        seasonLocalDataSource.updateEpisodeNotificationsEnabled(seasonId = seasonId, enabled = enabled)
    }

    override suspend fun getSeasonsWithEpisodeNotifications(): List<Season> =
        seasonLocalDataSource.getSeasonsWithEpisodeNotifications()

    override suspend fun updateLastEpisodeCheckDate(seasonId: Long, date: LocalDate) {
        seasonLocalDataSource.updateLastEpisodeCheckDate(seasonId = seasonId, date = date)
    }

    override fun observeWatchedEpisodesForSeason(seasonId: Long): Flow<Set<Int>> =
        watchedEpisodeLocalDataSource.observeWatchedEpisodeNumbers(seasonId)

    override suspend fun setEpisodeWatched(seasonId: Long, episodeNumber: Int, isWatched: Boolean) {
        if (isWatched) watchedEpisodeLocalDataSource.markWatched(seasonId, episodeNumber)
        else watchedEpisodeLocalDataSource.markUnwatched(seasonId, episodeNumber)
    }
}
