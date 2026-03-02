package com.vuzeda.animewatchlist.tracker.data.repository.impl

import com.vuzeda.animewatchlist.tracker.data.local.dao.SeasonDao
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toDomainModel
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.TransactionRunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SeasonRepositoryImpl @Inject constructor(
    private val seasonDao: SeasonDao,
    private val transactionRunner: TransactionRunner
) : SeasonRepository {

    override fun observeSeasonsForAnime(animeId: Long): Flow<List<Season>> =
        seasonDao.observeByAnimeId(animeId).map { entities -> entities.map { it.toDomainModel() } }

    override fun observeSeasonById(id: Long): Flow<Season?> =
        seasonDao.observeById(id).map { it?.toDomainModel() }

    override suspend fun findAnimeIdBySeasonMalId(malId: Int): Long? =
        seasonDao.findByMalId(malId)?.animeId

    override suspend fun findAnimeIdsBySeasonMalIds(malIds: List<Int>): Map<Int, Long> =
        seasonDao.findAnimeIdsByMalIds(malIds).associate { it.malId to it.animeId }

    override suspend fun getSeasonsForAnime(animeId: Long): List<Season> =
        seasonDao.getByAnimeId(animeId).map { it.toDomainModel() }

    override suspend fun addSeasonsToAnime(animeId: Long, seasons: List<Season>) {
        transactionRunner.runInTransaction {
            val seasonEntities = seasons.map { it.copy(animeId = animeId).toEntity() }
            seasonDao.insertAll(seasonEntities)
        }
    }

    override suspend fun updateSeason(season: Season) {
        seasonDao.update(season.toEntity())
    }

    override suspend fun updateSeasonNotificationData(
        seasonId: Long,
        lastCheckedAiredEpisodeCount: Int?
    ) {
        seasonDao.updateNotificationData(
            seasonId = seasonId,
            count = lastCheckedAiredEpisodeCount
        )
    }
}
