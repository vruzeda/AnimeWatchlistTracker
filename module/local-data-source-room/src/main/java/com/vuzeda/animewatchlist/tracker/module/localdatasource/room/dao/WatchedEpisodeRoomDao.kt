package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vuzeda.animewatchlist.tracker.module.localdatasource.WatchedEpisodeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.WatchedEpisodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
abstract class WatchedEpisodeRoomDao : WatchedEpisodeLocalDataSource {

    @Query("SELECT episodeNumber FROM watched_episode WHERE seasonId = :seasonId")
    abstract fun observeEpisodeNumbers(seasonId: Long): Flow<List<Int>>

    @Query("SELECT seasonId, COUNT(*) AS count FROM watched_episode GROUP BY seasonId")
    abstract fun observeWatchedCountsBySeasonId(): Flow<Map<@MapColumn("seasonId") Long, @MapColumn("count") Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertEntity(entity: WatchedEpisodeEntity)

    @Query("DELETE FROM watched_episode WHERE seasonId = :seasonId AND episodeNumber = :episodeNumber")
    abstract override suspend fun markUnwatched(seasonId: Long, episodeNumber: Int)

    override fun observeWatchedEpisodeNumbers(seasonId: Long): Flow<Set<Int>> =
        observeEpisodeNumbers(seasonId).map { it.toSet() }

    override fun observeWatchedCountsForAllSeasons(): Flow<Map<Long, Int>> =
        observeWatchedCountsBySeasonId()

    override suspend fun markWatched(seasonId: Long, episodeNumber: Int) =
        insertEntity(WatchedEpisodeEntity(seasonId = seasonId, episodeNumber = episodeNumber))
}
