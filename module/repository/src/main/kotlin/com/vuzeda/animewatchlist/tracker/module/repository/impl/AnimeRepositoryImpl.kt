package com.vuzeda.animewatchlist.tracker.module.repository.impl

import com.vuzeda.animewatchlist.tracker.module.localdatasource.AnimeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.mapper.toDomainModel
import com.vuzeda.animewatchlist.tracker.module.repository.mapper.toLocalModel
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import com.vuzeda.animewatchlist.tracker.module.repository.TransactionRunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val animeLocalDataSource: AnimeLocalDataSource,
    private val seasonRepository: SeasonRepository,
    private val transactionRunner: TransactionRunner
) : AnimeRepository {

    override fun observeAll(): Flow<List<Anime>> =
        animeLocalDataSource.observeAll().map { entities -> entities.map { it.toDomainModel() } }

    override fun observeByStatus(status: WatchStatus): Flow<List<Anime>> =
        animeLocalDataSource.observeByStatus(status.name).map { entities -> entities.map { it.toDomainModel() } }

    override fun observeById(id: Long): Flow<Anime?> =
        animeLocalDataSource.observeById(id).map { it?.toDomainModel() }

    override suspend fun getAnimeById(id: Long): Anime? =
        animeLocalDataSource.getById(id)?.toDomainModel()

    override suspend fun addAnime(anime: Anime, seasons: List<Season>): Long =
        transactionRunner.runInTransaction {
            val animeId = animeLocalDataSource.insert(anime.toLocalModel())
            seasonRepository.addSeasonsToAnime(animeId, seasons)
            animeId
        }

    override suspend fun updateAnime(anime: Anime) {
        animeLocalDataSource.update(anime.toLocalModel())
    }

    override suspend fun deleteAnime(id: Long) {
        animeLocalDataSource.deleteById(id)
    }

    override suspend fun updateNotificationType(id: Long, notificationType: NotificationType) {
        animeLocalDataSource.updateNotificationType(id = id, notificationType = notificationType.name)
    }

    override suspend fun getNotificationEnabledAnime(): List<Anime> =
        animeLocalDataSource.getNotificationEnabledAnime().map { it.toDomainModel() }

    override suspend fun deleteAllData() {
        transactionRunner.runInTransaction {
            animeLocalDataSource.deleteAll()
        }
    }
}
