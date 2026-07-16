package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.localdatasource.EpisodeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.EpisodeInfoEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toDomainModel
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.toEntity

@Dao
abstract class EpisodeInfoRoomDao : EpisodeLocalDataSource {

    @Query("SELECT * FROM episode_info WHERE malId = :malId ORDER BY number ASC")
    abstract suspend fun getAllEntities(malId: Int): List<EpisodeInfoEntity>

    @Upsert
    abstract suspend fun upsertAllEntities(entities: List<EpisodeInfoEntity>)

    override suspend fun getEpisodes(malId: Int): List<EpisodeInfo> =
        getAllEntities(malId).map { it.toDomainModel() }

    override suspend fun upsertEpisodes(malId: Int, episodes: List<EpisodeInfo>) =
        upsertAllEntities(episodes.map { it.toEntity(malId) })

    @Query("DELETE FROM episode_info WHERE malId NOT IN (SELECT malId FROM season)")
    abstract suspend fun deleteOrphanedEpisodes()

    override suspend fun deleteEpisodesNotInWatchlist() = deleteOrphanedEpisodes()
}
