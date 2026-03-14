package com.vuzeda.animewatchlist.tracker.data.repository.impl

import com.vuzeda.animewatchlist.tracker.data.local.SeasonLocalDataSource
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toDomainModel
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toLocalModel
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.TransactionRunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SeasonRepositoryImpl @Inject constructor(
    private val seasonLocalDataSource: SeasonLocalDataSource,
    private val transactionRunner: TransactionRunner
) : SeasonRepository {

    override fun observeAllSeasons(): Flow<List<Season>> =
        seasonLocalDataSource.observeAll().map { entities -> entities.map { it.toDomainModel() } }

    override fun observeAllSeasonMalIds(): Flow<Set<Int>> =
        seasonLocalDataSource.observeAllMalIds().map { it.toSet() }

    override fun observeSeasonsForAnime(animeId: Long): Flow<List<Season>> =
        seasonLocalDataSource.observeByAnimeId(animeId).map { entities -> entities.map { it.toDomainModel() } }

    override fun observeSeasonById(id: Long): Flow<Season?> =
        seasonLocalDataSource.observeById(id).map { it?.toDomainModel() }

    override suspend fun findAnimeIdBySeasonMalId(malId: Int): Long? =
        seasonLocalDataSource.findByMalId(malId)?.animeId

    override suspend fun findSeasonIdByMalId(malId: Int): Long? =
        seasonLocalDataSource.findByMalId(malId)?.id

    override suspend fun getSeasonsForAnime(animeId: Long): List<Season> =
        seasonLocalDataSource.getByAnimeId(animeId).map { it.toDomainModel() }

    override suspend fun addSeasonsToAnime(animeId: Long, seasons: List<Season>) {
        transactionRunner.runInTransaction {
            val localSeasons = seasons.map { it.copy(animeId = animeId).toLocalModel() }
            seasonLocalDataSource.insertAll(localSeasons)
        }
    }

    override suspend fun updateSeason(season: Season) {
        seasonLocalDataSource.update(season.toLocalModel())
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
        seasonLocalDataSource.getSeasonsWithEpisodeNotifications().map { it.toDomainModel() }
}
