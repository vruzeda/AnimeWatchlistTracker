package com.vuzeda.animewatchlist.tracker.data.repository.impl

import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.dao.SeasonDao
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toDomainModel
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val animeDao: AnimeDao,
    private val seasonDao: SeasonDao
) : AnimeRepository {

    override fun observeAll(): Flow<List<Anime>> =
        animeDao.observeAll().map { entities -> entities.map { it.toDomainModel() } }

    override fun observeByStatus(status: WatchStatus): Flow<List<Anime>> =
        animeDao.observeByStatus(status.name).map { entities -> entities.map { it.toDomainModel() } }

    override fun observeById(id: Long): Flow<Anime?> =
        animeDao.observeById(id).map { it?.toDomainModel() }

    override fun observeSeasonsForAnime(animeId: Long): Flow<List<Season>> =
        seasonDao.observeByAnimeId(animeId).map { entities -> entities.map { it.toDomainModel() } }

    override fun observeSeasonById(id: Long): Flow<Season?> =
        seasonDao.observeById(id).map { it?.toDomainModel() }

    override suspend fun findAnimeIdBySeasonMalId(malId: Int): Long? =
        seasonDao.findByMalId(malId)?.animeId

    override suspend fun addAnime(anime: Anime, seasons: List<Season>): Long {
        val animeId = animeDao.insert(anime.toEntity())
        val seasonEntities = seasons.map { it.copy(animeId = animeId).toEntity() }
        seasonDao.insertAll(seasonEntities)
        return animeId
    }

    override suspend fun updateAnime(anime: Anime) {
        animeDao.update(anime.toEntity())
    }

    override suspend fun updateSeason(season: Season) {
        seasonDao.update(season.toEntity())
    }

    override suspend fun deleteAnime(id: Long) {
        animeDao.deleteById(id)
    }

    override suspend fun toggleNotifications(id: Long, enabled: Boolean) {
        animeDao.updateNotificationsEnabled(id = id, enabled = if (enabled) 1 else 0)
    }

    override suspend fun getNotificationEnabledAnime(): List<Anime> =
        animeDao.getNotificationEnabledAnime().map { it.toDomainModel() }

    override suspend fun getSeasonsForAnime(animeId: Long): List<Season> =
        seasonDao.getByAnimeId(animeId).map { it.toDomainModel() }

    override suspend fun updateSeasonNotificationData(
        seasonId: Long,
        lastCheckedAiredEpisodeCount: Int?
    ) {
        seasonDao.updateNotificationData(
            seasonId = seasonId,
            count = lastCheckedAiredEpisodeCount
        )
    }

    override suspend fun addSeasonsToAnime(animeId: Long, seasons: List<Season>) {
        val seasonEntities = seasons.map { it.copy(animeId = animeId).toEntity() }
        seasonDao.insertAll(seasonEntities)
    }

    override suspend fun deleteAllData() {
        animeDao.deleteAll()
    }
}
