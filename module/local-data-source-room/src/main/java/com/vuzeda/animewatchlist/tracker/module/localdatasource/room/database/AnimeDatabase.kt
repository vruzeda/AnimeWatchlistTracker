package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.AnimeRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.SeasonRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.WatchedEpisodeRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.AnimeUpdateSchedulerStateEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.SeasonEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.WatchedEpisodeEntity

@Database(
    entities = [AnimeEntity::class, SeasonEntity::class, AnimeUpdateSchedulerStateEntity::class, WatchedEpisodeEntity::class],
    version = 17,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeRoomDao
    abstract fun seasonDao(): SeasonRoomDao
    abstract fun watchedEpisodeDao(): WatchedEpisodeRoomDao
}
