package com.vuzeda.animewatchlist.tracker.data.repository.impl

import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toDomainModel
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.domain.repository.TransactionRunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val animeDao: AnimeDao,
    private val seasonRepository: SeasonRepository,
    private val transactionRunner: TransactionRunner
) : AnimeRepository {

    override fun observeAll(): Flow<List<Anime>> =
        animeDao.observeAll().map { entities -> entities.map { it.toDomainModel() } }

    override fun observeByStatus(status: WatchStatus): Flow<List<Anime>> =
        animeDao.observeByStatus(status.name).map { entities -> entities.map { it.toDomainModel() } }

    override fun observeById(id: Long): Flow<Anime?> =
        animeDao.observeById(id).map { it?.toDomainModel() }

    override fun observeByNotificationEnabled(enabled: Boolean): Flow<List<Anime>> =
        animeDao.observeByNotificationEnabled(enabled)
            .map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun addAnime(anime: Anime, seasons: List<Season>): Long =
        transactionRunner.runInTransaction {
            val animeId = animeDao.insert(anime.toEntity())
            seasonRepository.addSeasonsToAnime(animeId, seasons)
            animeId
        }

    override suspend fun updateAnime(anime: Anime) {
        animeDao.update(anime.toEntity())
    }

    override suspend fun deleteAnime(id: Long) {
        animeDao.deleteById(id)
    }

    override suspend fun toggleNotifications(id: Long, enabled: Boolean) {
        animeDao.updateNotificationsEnabled(id = id, enabled = enabled)
    }

    override suspend fun getNotificationEnabledAnime(): List<Anime> =
        animeDao.getNotificationEnabledAnime().map { it.toDomainModel() }

    override suspend fun deleteAllData() {
        transactionRunner.runInTransaction {
            animeDao.deleteAll()
        }
    }
}
