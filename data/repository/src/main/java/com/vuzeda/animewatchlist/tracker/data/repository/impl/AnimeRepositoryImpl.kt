package com.vuzeda.animewatchlist.tracker.data.repository.impl

import com.vuzeda.animewatchlist.tracker.data.api.service.JikanApiService
import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toDomainModel
import com.vuzeda.animewatchlist.tracker.data.repository.mapper.toEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val animeDao: AnimeDao,
    private val jikanApiService: JikanApiService
) : AnimeRepository {

    override fun observeWatchlist(): Flow<List<Anime>> =
        animeDao.observeAll().map { entities -> entities.map { it.toDomainModel() } }

    override fun observeWatchlistByStatus(status: WatchStatus): Flow<List<Anime>> =
        animeDao.observeByStatus(status.name).map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getAnimeById(id: Long): Anime? =
        animeDao.getById(id)?.toDomainModel()

    override suspend fun addAnime(anime: Anime): Long =
        animeDao.insert(anime.toEntity())

    override suspend fun updateAnime(anime: Anime) {
        animeDao.update(anime.toEntity())
    }

    override suspend fun deleteAnime(id: Long) {
        animeDao.deleteById(id)
    }

    override suspend fun searchAnime(query: String): Result<List<Anime>> = runCatching {
        jikanApiService.searchAnime(query = query).data.map { it.toDomainModel() }
    }
}
