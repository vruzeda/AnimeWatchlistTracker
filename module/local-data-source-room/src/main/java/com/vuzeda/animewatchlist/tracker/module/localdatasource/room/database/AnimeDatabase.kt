package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.AnimeRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.SeasonRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.SeasonEntity

@Database(
    entities = [AnimeEntity::class, SeasonEntity::class],
    version = 12,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeRoomDao
    abstract fun seasonDao(): SeasonRoomDao
}
