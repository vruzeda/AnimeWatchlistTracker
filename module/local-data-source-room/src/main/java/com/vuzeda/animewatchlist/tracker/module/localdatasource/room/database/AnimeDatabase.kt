package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.AnimeRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.EpisodeInfoRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.SeasonRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.WatchedEpisodeRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.AnimeUpdateSchedulerStateEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.EpisodeInfoEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.SeasonEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.WatchedEpisodeEntity

@DeleteColumn(tableName = "season", columnName = "currentEpisode")
class SeasonDropCurrentEpisodeMigration : AutoMigrationSpec

@Database(
    entities = [AnimeEntity::class, SeasonEntity::class, AnimeUpdateSchedulerStateEntity::class, WatchedEpisodeEntity::class, EpisodeInfoEntity::class],
    version = 20,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 19, to = 20, spec = SeasonDropCurrentEpisodeMigration::class)
    ]
)
@TypeConverters(Converters::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeRoomDao
    abstract fun seasonDao(): SeasonRoomDao
    abstract fun watchedEpisodeDao(): WatchedEpisodeRoomDao
    abstract fun episodeInfoDao(): EpisodeInfoRoomDao
}
